package org.batfish.question.vendorspecific;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.vendor.VendorConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VendorSpecificConfigurationAnswerer extends Answerer {
    static final String COL_FILE_NAME = "File_Name";
    static final String COL_ROUTE_MAPS = "Route_Maps";
    static final String COL_STANDARD_COMMUNITY_LISTS = "Standard_Community_Lists";
    static final String COL_INTERFACES = "Interfaces";
    static final String COL_VRF = "Vrf";
    static final String COL_IP_LISTS = "Ip_Lists";

    public static TableMetadata createMetadata() {
        List<ColumnMetadata> columnMetadata =
                ImmutableList.of(
                        new ColumnMetadata(
                                COL_FILE_NAME, Schema.STRING, "The file name of this configuration", true, false),
                        new ColumnMetadata(
                                COL_ROUTE_MAPS, Schema.OBJECT, "The definition of route maps", false, true),
                        new ColumnMetadata(
                                COL_STANDARD_COMMUNITY_LISTS, Schema.OBJECT, "The standard community lists", false, true),
                        new ColumnMetadata(
                                COL_INTERFACES, Schema.OBJECT, "The list of interfaces", false, true),
                        new ColumnMetadata(
                                COL_VRF, Schema.OBJECT, "The default vrf", false, true),
                        new ColumnMetadata(COL_IP_LISTS, Schema.OBJECT, "The list of ips", false, true)
                );

        return new TableMetadata(
                columnMetadata,
                String.format( "Configuration defined in file {%s}", COL_FILE_NAME));
    }

    public VendorSpecificConfigurationAnswerer(Question question, IBatfish batfish) {
        super(question, batfish);
    }

    @Override
    public TableAnswerElement answer(NetworkSnapshot snapshot) {
        VendorSpecificConfigurationQuestion question = (VendorSpecificConfigurationQuestion) _question;
        Set<String> includeNodes = question.getNodes().getMatchingNodes(_batfish, snapshot);
        Multimap<String, String> hostnameFilenameMap =
                _batfish.loadParseVendorConfigurationAnswerElement(snapshot).getFileMap();
        Set<String> includeFiles =
                hostnameFilenameMap.entries().stream()
                        .filter(e -> includeNodes.contains(e.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toSet());
        Pattern includeStructureNames = Pattern.compile(question.getNames(), Pattern.CASE_INSENSITIVE);

        TableMetadata tableMetadata = createMetadata();
        Map<String, ColumnMetadata> columns = tableMetadata.toColumnMap();
        ImmutableList.Builder<Row> rows = ImmutableList.builder();

        Map<String, VendorConfiguration> configurationMap = _batfish.loadVendorConfigurations(snapshot);
        configurationMap.forEach(
            (name, config) -> {
                if (!includeStructureNames.matcher(name).matches()) { return; }

                CiscoConfiguration ciscoConfig = (CiscoConfiguration) config;
                if (ciscoConfig == null) {
                    System.out.println("Skipping " + name + " because it is not a cisco configuration");
                    return;
                }

                Row.RowBuilder row = Row.builder(columns).put(COL_FILE_NAME, name);
                row.put(COL_ROUTE_MAPS, ciscoConfig.getRouteMaps());
                row.put(COL_STANDARD_COMMUNITY_LISTS, ciscoConfig.getStandardCommunityLists());
                row.put(COL_INTERFACES, ciscoConfig.getInterfaces());
                row.put(COL_VRF, ciscoConfig.getDefaultVrf());
                row.put(COL_IP_LISTS, ciscoConfig.getPrefixLists());

                rows.add(row.build());
            }
        );


        TableAnswerElement answer = new TableAnswerElement(tableMetadata);
        answer.postProcessAnswer(question, rows.build());
        return answer;
    }
}