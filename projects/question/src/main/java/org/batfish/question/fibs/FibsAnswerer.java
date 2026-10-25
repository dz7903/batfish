package org.batfish.question.fibs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibAction;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * Answers {@link FibsQuestion}: one row per FIB entry, giving the top-level route (the main-RIB
 * route the entry comes from) and the forwarding action its next hop resolves to.
 */
@ParametersAreNonnullByDefault
public class FibsAnswerer extends Answerer {

  static final String COL_NODE = "Node";
  static final String COL_VRF = "VRF";
  static final String COL_NETWORK = "Network";
  static final String COL_PROTOCOL = "Protocol";
  static final String COL_NEXT_HOP = "Next_Hop";
  static final String COL_ACTION = "Action";
  static final String COL_INTERFACE = "Interface";
  static final String COL_ARP_IP = "Arp_IP";
  static final String COL_NEXT_VRF = "Next_VRF";

  private final Map<String, ColumnMetadata> _columnMap;

  public FibsAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
    _columnMap = getMetadata().toColumnMap();
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    FibsQuestion q = (FibsQuestion) _question;
    TableAnswerElement answer = new TableAnswerElement(getMetadata());
    answer.postProcessAnswer(
        _question,
        getRows(
            _batfish.loadDataPlane(snapshot).getFibs(),
            q.getNodeSpecifier().resolve(_batfish.specifierContext(snapshot)),
            Pattern.compile(q.getVrfs()),
            _columnMap));
    return answer;
  }

  @VisibleForTesting
  static List<Row> getRows(
      Map<String, Map<String, Fib>> fibs,
      Set<String> nodes,
      Pattern vrfRegex,
      Map<String, ColumnMetadata> columnMap) {
    ImmutableList.Builder<Row> rows = ImmutableList.builder();
    for (String node : ImmutableSortedSet.copyOf(fibs.keySet())) {
      if (!nodes.contains(node)) {
        continue;
      }
      Map<String, Fib> nodeFibs = fibs.get(node);
      for (String vrf : ImmutableSortedSet.copyOf(nodeFibs.keySet())) {
        if (!vrfRegex.matcher(vrf).matches()) {
          continue;
        }
        nodeFibs.get(vrf).allEntries().stream()
            .map(entry -> toRow(node, vrf, entry, columnMap))
            .sorted(Comparator.comparing(Row::toString))
            .forEach(rows::add);
      }
    }
    return rows.build();
  }

  private static Row toRow(
      String node, String vrf, FibEntry entry, Map<String, ColumnMetadata> columnMap) {
    AbstractRoute top = entry.getTopLevelRoute();
    FibAction action = entry.getAction();
    return Row.builder(columnMap)
        .put(COL_NODE, new Node(node))
        .put(COL_VRF, vrf)
        .put(COL_NETWORK, top.getNetwork())
        .put(COL_PROTOCOL, top.getProtocol().protocolName())
        .put(COL_NEXT_HOP, top.getNextHop())
        .put(COL_ACTION, action.accept(ACTION_NAME))
        .put(COL_INTERFACE, action instanceof FibForward f ? f.getInterfaceName() : null)
        .put(COL_ARP_IP, arpIp(action))
        .put(COL_NEXT_VRF, action instanceof FibNextVrf v ? v.getNextVrf() : null)
        .build();
  }

  private static @Nullable Ip arpIp(FibAction action) {
    return action instanceof FibForward f ? f.getArpIp().orElse(null) : null;
  }

  private static final FibActionVisitor<String> ACTION_NAME =
      new FibActionVisitor<>() {
        @Override
        public String visitFibForward(FibForward fibForward) {
          return "forward";
        }

        @Override
        public String visitFibNextVrf(FibNextVrf fibNextVrf) {
          return "next_vrf";
        }

        @Override
        public String visitFibNullRoute(FibNullRoute fibNullRoute) {
          return "discard";
        }
      };

  private static TableMetadata getMetadata() {
    return new TableMetadata(getColumnMetadata());
  }

  @VisibleForTesting
  static List<ColumnMetadata> getColumnMetadata() {
    return ImmutableList.of(
        new ColumnMetadata(COL_NODE, Schema.NODE, "Node of the FIB", true, false),
        new ColumnMetadata(COL_VRF, Schema.STRING, "VRF of the FIB", true, false),
        new ColumnMetadata(
            COL_NETWORK, Schema.PREFIX, "Network of the top-level route", true, false),
        new ColumnMetadata(
            COL_PROTOCOL, Schema.STRING, "Protocol of the top-level route", true, false),
        new ColumnMetadata(
            COL_NEXT_HOP, Schema.NEXT_HOP, "Next hop of the top-level route", true, false),
        new ColumnMetadata(COL_ACTION, Schema.STRING, "forward, discard, or next_vrf", true, false),
        new ColumnMetadata(
            COL_INTERFACE, Schema.STRING, "Outgoing interface, for forward", true, false),
        new ColumnMetadata(COL_ARP_IP, Schema.IP, "IP to ARP for, for forward", true, false),
        new ColumnMetadata(
            COL_NEXT_VRF, Schema.STRING, "VRF to look up in, for next_vrf", true, false));
  }
}
