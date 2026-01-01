package org.batfish.question.vendorspecific;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.vendorspecific.ir.DenyAction;
import org.batfish.question.vendorspecific.ir.Interface;
import org.batfish.question.vendorspecific.ir.PermitAction;
import org.batfish.question.vendorspecific.ir.Policy;
import org.batfish.question.vendorspecific.ir.PolicySet;
import org.batfish.representation.cisco.BgpPeerGroup;
import org.batfish.representation.cisco.BgpProcess;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.representation.cisco.IpBgpPeerGroup;
import org.batfish.representation.juniper.BgpGroup;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.RoutingInstance;
import org.batfish.representation.juniper.IpBgpGroup;
import org.batfish.vendor.VendorConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class VendorSpecificConfigurationAnswerer extends Answerer {
    public static void warn(String message, Object... args) {
        System.out.println("Warning: " + String.format(message, args));
    }

    static final String COL_FILE_NAME = "File_Name";
    static final String COL_CONFIG_FORMAT = "Config_Format";
    static final String COL_AS_NUM = "As_Num";
    static final String COL_INTERFACES = "Interfaces";

    public static TableMetadata createMetadata() {
        List<ColumnMetadata> columnMetadata =
                ImmutableList.of(
                        new ColumnMetadata(
                                COL_FILE_NAME, Schema.STRING, "The file name of this configuration", true, false),
                        new ColumnMetadata(
                                COL_CONFIG_FORMAT, Schema.STRING, "Format of the configuration", false, true),
                        new ColumnMetadata(
                                COL_AS_NUM, Schema.INTEGER, "AS number", false, true),
                        new ColumnMetadata(
                                COL_INTERFACES, Schema.OBJECT, "The list of interfaces", false, true)
                );

        return new TableMetadata(
                columnMetadata,
                String.format( "Configuration defined in file {%s}", COL_FILE_NAME));
    }

    public VendorSpecificConfigurationAnswerer(Question question, IBatfish batfish) {
        super(question, batfish);
    }

    private Row processCisco(String name, CiscoConfiguration config) {
        Row.RowBuilder row = Row.builder();
        row.put(COL_FILE_NAME, name);
        row.put(COL_CONFIG_FORMAT, "cisco");

        BgpProcess process = config.getDefaultVrf().getBgpProcess();
        if (process == null)
            throw new IllegalArgumentException("default VRF not found");

        long asNum = process.getProcnum();
        row.put(COL_AS_NUM, asNum);
        Ip routerId = process.getRouterId();

        Map<String, Policy> routeMaps = new HashMap<>();
        List<Interface> interfaces = new ArrayList<>();
        for (IpBgpPeerGroup ipPeerGroup : process.getIpPeerGroups().values()) {
//            if (name.equals("as2dist2"))
//                System.out.println("ip peer group " + ipPeerGroup.getName());
            BgpPeerGroup peerGroup = ipPeerGroup;
            Long localAs = null, remoteAs = null;
            PolicySet importPolicies = null, exportPolicies = null;
            while (peerGroup != null) {
                if (localAs == null)
                    localAs = peerGroup.getLocalAs();
                if (remoteAs == null)
                    remoteAs = peerGroup.getRemoteAs();
                if (importPolicies == null) {
                    if(peerGroup.getInboundRouteMap() != null) {
                        importPolicies = new PolicySet(new ArrayList<>(), new DenyAction());
                        if (routeMaps.get(peerGroup.getInboundRouteMap()) == null) {
                            routeMaps.put(peerGroup.getInboundRouteMap(), Convert.convertCiscoPolicy(config, config.getRouteMaps().get(peerGroup.getInboundRouteMap())));
                        }
                        importPolicies.policies.add(routeMaps.get(peerGroup.getInboundRouteMap()));
                    }
                }
                if (exportPolicies == null) {
                    if(peerGroup.getOutboundRouteMap() != null) {
                        exportPolicies = new PolicySet(new ArrayList<>(), new DenyAction());
                        if (routeMaps.get(peerGroup.getOutboundRouteMap()) == null) {
                            routeMaps.put(peerGroup.getOutboundRouteMap(), Convert.convertCiscoPolicy(config, config.getRouteMaps().get(peerGroup.getOutboundRouteMap())));
                        }
                        exportPolicies.policies.add(routeMaps.get(peerGroup.getOutboundRouteMap()));
                    }
                }
                peerGroup = process.getNamedPeerGroups().get(peerGroup.getGroupName());
            }
            if (localAs == null) localAs = asNum;
            if (remoteAs == null) {
                warn("unknown remote as for " + ipPeerGroup.getGroupName() + " " + ipPeerGroup.getName() + ", will use local as");
                remoteAs = asNum;
            }

            Ip remoteIp = ipPeerGroup.getIp();
            Ip localIp = null;

            for (var interf : config.getInterfaces().keySet()) {
                var addr = config.getInterfaces().get(interf).getAddress();
                if (addr == null) continue;
                if (addr.getPrefix().containsIp(remoteIp)) {
                    if (localIp != null)
                        warn("multiple possible remote IP: " + localIp + " and " + addr.getIp() + " in " + name);
                    localIp = addr.getIp();
                }
            }
            if (localIp == null) localIp = routerId;

            interfaces.add(new Interface(localIp, localAs, remoteIp, remoteAs, asNum == remoteAs, importPolicies, exportPolicies));
        }
        row.put(COL_INTERFACES, interfaces);

        return row.build();
    }
 
    private Row processJuniper(String name, JuniperConfiguration config) {
        Row.RowBuilder row = Row.builder();
        row.put(COL_FILE_NAME, name);
        row.put(COL_CONFIG_FORMAT, "juniper");

        long asNum = config.getMasterLogicalSystem().getDefaultRoutingInstance().getAs();
        row.put(COL_AS_NUM, asNum);

        Map<String, Policy> routeMaps = new HashMap<>();
        List<Interface> interfaces = new ArrayList<>();
        RoutingInstance instance = config.getMasterLogicalSystem().getDefaultRoutingInstance();
//      Bagpipe and Timepiece only verify the default VRF, so we also skip non-default VRFs here
//        for (RoutingInstance instance : config.getMasterLogicalSystem().getRoutingInstances().values()) {
        for (IpBgpGroup ig : instance.getIpBgpGroups().values()) {
            Long localAs = null, remoteAs = null;
            Ip localIp = null;
            PolicySet importPolicies = null, exportPolicies = null;
            Boolean isInternal = null;

            BgpGroup bg = ig;
            StringBuilder groupName = new StringBuilder().append(ig.getRemoteAddress().getStartIp());
            while (bg != null) {
                if (bg.getGroupName() != null) {
                    groupName.append(" in ").append(bg.getGroupName());
                }
                if (localAs == null && bg.getLocalAs() != null) {
                    localAs = bg.getLocalAs();
                }
                if (remoteAs == null && bg.getPeerAs() != null) {
                    remoteAs = bg.getPeerAs();
                }
                if (localIp == null && bg.getLocalAddress() != null) {
                    localIp = bg.getLocalAddress();
                }
                if (isInternal == null && bg.getType() != null) {
                    isInternal = bg.getType() == BgpGroup.BgpGroupType.INTERNAL;
                }
                if (importPolicies == null && !bg.getImportPolicies().isEmpty()) {
                    importPolicies = new PolicySet(new ArrayList<>(), new PermitAction());
                    for (String importPolicy : bg.getImportPolicies()) {
                        if (routeMaps.get(importPolicy) == null) {
                            routeMaps.put(importPolicy, Convert.convertJuniperPolicy(config, config.getMasterLogicalSystem().getPolicyStatements().get(importPolicy)));
                        }
                        importPolicies.policies.add(routeMaps.get(importPolicy));
                    }
                }
                if (exportPolicies == null && !bg.getExportPolicies().isEmpty()) {
                    exportPolicies = new PolicySet(new ArrayList<>(), new PermitAction());
                    for (String exportPolicy : bg.getExportPolicies()) {
                        if (routeMaps.get(exportPolicy) == null) {
                            routeMaps.put(exportPolicy, Convert.convertJuniperPolicy(config, config.getMasterLogicalSystem().getPolicyStatements().get(exportPolicy)));
                        }
                        exportPolicies.policies.add(routeMaps.get(exportPolicy));
                    }
                }
                bg = bg.getParent();
            }
            groupName.append(" in ").append(name);

            if (ig.getRemoteAddress().getPrefixLength() != Prefix.MAX_PREFIX_LENGTH) {
                throw new IllegalArgumentException("prefix for remote address not supported for group " + groupName);
            }
            Ip remoteIp = ig.getRemoteAddress().getStartIp();

            if (isInternal == null) {
                isInternal = false;
            }
            if (localAs == null) {
                localAs = instance.getAs();
            }
            if (localAs == null) {
                localAs = instance.getMasterBgpGroup().getLocalAs();
            }
            if (localAs == null) {
                localAs = asNum;
            }
            if (remoteAs == null && isInternal) {
                remoteAs = localAs;
            }

            if (remoteAs == null) {
                throw new IllegalArgumentException("remote as not found for group " + groupName);
            }
            if (localIp == null) {
                if (isInternal) {
                    throw new IllegalArgumentException("local IP not found for group " + groupName);
                } else {
                    warn("local IP not found for group " + groupName);
                }
            }
            interfaces.add(new Interface(localIp, localAs, remoteIp, remoteAs, isInternal, importPolicies, exportPolicies));
        }
//        }
        row.put(COL_INTERFACES, interfaces);
        return row.build();
    }

    @Override
    public TableAnswerElement answer(NetworkSnapshot snapshot) {
        VendorSpecificConfigurationQuestion question = (VendorSpecificConfigurationQuestion) _question;
        Pattern includeStructureNames = Pattern.compile(question.getNames(), Pattern.CASE_INSENSITIVE);

        TableMetadata tableMetadata = createMetadata();
        ImmutableList.Builder<Row> rows = ImmutableList.builder();
        Map<String, VendorConfiguration> configurationMap = _batfish.loadVendorConfigurations(snapshot);
        configurationMap.forEach(
            (name, config) -> {
                if (!includeStructureNames.matcher(name).matches()) { return; }

                if (config instanceof CiscoConfiguration ciscoConfig) {
                    rows.add(processCisco(name, ciscoConfig));
                } else if (config instanceof JuniperConfiguration juniperConfig) {
                    rows.add(processJuniper(name, juniperConfig));
                } else {
                    warn("Skipping %s because it is not a cisco or juniper configuration", name);
                }
            }
        );

        TableAnswerElement answer = new TableAnswerElement(tableMetadata);
        answer.postProcessAnswer(question, rows.build());
        return answer;
    }
}