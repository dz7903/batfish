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
import org.batfish.question.vendorspecific.ir.Interface;
import org.batfish.question.vendorspecific.ir.RouteMap;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.representation.cisco.IpBgpPeerGroup;
import org.batfish.representation.juniper.BgpGroup;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.NamedBgpGroup;
import org.batfish.representation.juniper.PolicyStatement;
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
//        System.out.println("Warning: " + String.format(message, args));
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

        long asNum = config.getDefaultVrf().getBgpProcess().getProcnum();
        row.put(COL_AS_NUM, asNum);

        Map<String, RouteMap> routeMaps = new HashMap<>();
        for (Map.Entry<String, org.batfish.representation.cisco.RouteMap> entry : config.getRouteMaps().entrySet()) {
            routeMaps.put(entry.getKey(), Convert.convertCiscoRouteMap(config, entry.getValue()));
        }

        List<Interface> interfaces = new ArrayList<>();
        for (IpBgpPeerGroup peerGroup : config.getDefaultVrf().getBgpProcess().getIpPeerGroups().values()) {
            long remoteAs = peerGroup.getRemoteAs();
            Ip remoteIp = peerGroup.getIp();

            ArrayList<RouteMap> importRouteMaps = new ArrayList<>();
            if(routeMaps.get(peerGroup.getInboundRouteMap()) != null) {
                importRouteMaps.add(routeMaps.get(peerGroup.getInboundRouteMap()));
            }

            ArrayList<RouteMap> exportRouteMaps = new ArrayList<>();
            if(routeMaps.get(peerGroup.getOutboundRouteMap()) != null) {
                exportRouteMaps.add(routeMaps.get(peerGroup.getOutboundRouteMap()));
            }

            interfaces.add(new Interface(null, asNum, remoteIp, remoteAs, asNum == remoteAs, importRouteMaps, exportRouteMaps));
        }
        row.put(COL_INTERFACES, interfaces);

        return row.build();
    }
 
    private Row processJuniper(String name, JuniperConfiguration config) {
        Row.RowBuilder row = Row.builder();
        row.put(COL_FILE_NAME, name);
        row.put(COL_CONFIG_FORMAT, "juniper");

        Long asNum = config.getMasterLogicalSystem().getDefaultRoutingInstance().getAs();

        Map<String, RouteMap> routeMaps = new HashMap<>();
        for (Map.Entry<String, PolicyStatement> entry: config.getMasterLogicalSystem().getPolicyStatements().entrySet()) {
            routeMaps.put(entry.getKey(), Convert.convertJuniperRouteMap(config, entry.getValue()));
        }

        List<Interface> interfaces = new ArrayList<>();
        for (RoutingInstance instance : config.getMasterLogicalSystem().getRoutingInstances().values()) {
            System.out.println(String.format("%s routing instance %s", name, instance.getName()));

            for (IpBgpGroup ig : instance.getIpBgpGroups().values()) {
                if (asNum == null) {
                    asNum = ig.getLocalAs();
                } else if (ig.getLocalAs() != null && !asNum.equals(ig.getLocalAs())) {
                    warn("multiple local-as %d,%d detected in %s", asNum, ig.getLocalAs(), name);
                }

                Ip localIp = ig.getLocalAddress();
                if (localIp != null) System.out.println(String.format("%s local address %s", name, localIp.toString()));
                if (ig.getRemoteAddress().getPrefixLength() != Prefix.MAX_PREFIX_LENGTH) {
                    warn("prefix for remote address not supported");
                }
                Ip remoteIp = ig.getRemoteAddress().getStartIp();

                Long localAs = ig.getLocalAs();
                Long remoteAs = ig.getPeerAs();
                boolean isInternal = ig.getType() == BgpGroup.BgpGroupType.INTERNAL;

                ArrayList<RouteMap> importRouteMaps = new ArrayList<>();
                for (String importPolicy : ig.getImportPolicies()) {
                    RouteMap routeMap = routeMaps.get(importPolicy);
                    importRouteMaps.add(routeMap);
                }

                ArrayList<RouteMap> exportRouteMaps = new ArrayList<>();
                for (String exportPolicy : ig.getExportPolicies()) {
                    RouteMap routeMap = routeMaps.get(exportPolicy);
                    exportRouteMaps.add(routeMap);
                }
                interfaces.add(new Interface(localIp, localAs, remoteIp, remoteAs, isInternal, importRouteMaps, exportRouteMaps));
            }

            if (!instance.getNamedBgpGroups().isEmpty()) {
                warn("ignoring named bgp group in routing instance %s inside %s", instance.getName(), name);
            }

            for (NamedBgpGroup bgpGroup : instance.getNamedBgpGroups().values()) {
                System.out.println(String.format("%s named bgp group %s", name, bgpGroup.getName()));
                System.out.println(String.format("    local address %s", bgpGroup.getLocalAddress()));
                System.out.println(String.format("    %s", bgpGroup.getGroupName()));
                System.out.println(String.format("    %s", bgpGroup.getParent().getGroupName()));
            }
        }
        row.put(COL_INTERFACES, interfaces);
        if (asNum == null) {
            warn("no local-as found in %s", name);
        }
        row.put(COL_AS_NUM, asNum);
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

                if (config instanceof CiscoConfiguration) {
                    CiscoConfiguration ciscoConfig = (CiscoConfiguration) config;
                    rows.add(processCisco(name, ciscoConfig));
                } else if (config instanceof JuniperConfiguration) {
                    JuniperConfiguration juniperConfig = (JuniperConfiguration) config;
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