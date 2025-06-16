package org.batfish.question.vendorspecific;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.question.vendorspecific.ir.Action;
import org.batfish.question.vendorspecific.ir.AddCommunity;
import org.batfish.question.vendorspecific.ir.Clause;
import org.batfish.question.vendorspecific.ir.CommunityList;
import org.batfish.question.vendorspecific.ir.DeleteCommunity;
import org.batfish.question.vendorspecific.ir.DenyAction;
import org.batfish.question.vendorspecific.ir.Match;
import org.batfish.question.vendorspecific.ir.MatchCommunity;
import org.batfish.question.vendorspecific.ir.MatchNothing;
import org.batfish.question.vendorspecific.ir.MatchPrefix;
import org.batfish.question.vendorspecific.ir.NextClauseAction;
import org.batfish.question.vendorspecific.ir.NextPolicyAction;
import org.batfish.question.vendorspecific.ir.NormalCommunityList;
import org.batfish.question.vendorspecific.ir.PermitAction;
import org.batfish.question.vendorspecific.ir.RegexCommunityList;
import org.batfish.question.vendorspecific.ir.RouteMap;
import org.batfish.question.vendorspecific.ir.SetCommunity;
import org.batfish.question.vendorspecific.ir.SetLocalPreference;
import org.batfish.question.vendorspecific.ir.Setter;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.representation.cisco.ExpandedCommunityList;
import org.batfish.representation.cisco.ExpandedCommunityListLine;
import org.batfish.representation.cisco.PrefixListLine;
import org.batfish.representation.cisco.RouteMapClause;
import org.batfish.representation.cisco.RouteMapMatchCommunityListLine;
//import org.batfish.representation.cisco.RouteMapMatchExtcommunityLine;
import org.batfish.representation.cisco.RouteMapMatchIpPrefixListLine;
import org.batfish.representation.cisco.RouteMapMatchLine;
import org.batfish.representation.cisco.RouteMapSetAdditiveCommunityLine;
import org.batfish.representation.cisco.RouteMapSetCommunityLine;
import org.batfish.representation.cisco.RouteMapSetDeleteCommunityLine;
import org.batfish.representation.cisco.RouteMapSetLine;
import org.batfish.representation.cisco.RouteMapSetLocalPreferenceLine;
import org.batfish.representation.cisco.StandardCommunityList;
import org.batfish.representation.cisco.StandardCommunityListLine;
import org.batfish.representation.juniper.CommunityMember;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.LiteralCommunityMember;
import org.batfish.representation.juniper.NamedCommunity;
import org.batfish.representation.juniper.PolicyStatement;
import org.batfish.representation.juniper.PrefixList;
import org.batfish.representation.juniper.PsFromCommunity;
import org.batfish.representation.juniper.PsFromPrefixList;
import org.batfish.representation.juniper.PsFromPrefixListFilterLonger;
import org.batfish.representation.juniper.PsFromPrefixListFilterOrLonger;
import org.batfish.representation.juniper.PsFroms;
import org.batfish.representation.juniper.PsTerm;
import org.batfish.representation.juniper.PsThen;
import org.batfish.representation.juniper.PsThenAccept;
import org.batfish.representation.juniper.PsThenCommunityAdd;
import org.batfish.representation.juniper.PsThenCommunityDelete;
import org.batfish.representation.juniper.PsThenCommunitySet;
import org.batfish.representation.juniper.PsThenLocalPreference;
import org.batfish.representation.juniper.PsThenNextPolicy;
import org.batfish.representation.juniper.PsThenReject;
import org.batfish.representation.juniper.RegexCommunityMember;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Convert {
    private static void warn(String message, Object ... args) {
        VendorSpecificConfigurationAnswerer.warn(message, args);
    }

    private static List<Set<String>> convertCiscoCommunityList(CiscoConfiguration config, String listName) {
        StandardCommunityList communityList = config.getStandardCommunityLists().get(listName);
        ExpandedCommunityList expandedCommunityList = config.getExpandedCommunityLists().get(listName);
        if (communityList == null && expandedCommunityList == null) {
            throw new IllegalArgumentException("can't find community list " + listName);
        }

        Set<String> communities = new HashSet<>();
        if (communityList != null) {
            for (StandardCommunityListLine communityListLine : communityList.getLines()) {
                if (communityListLine.getAction() != LineAction.PERMIT) {
                    throw new IllegalArgumentException("DENY in community list " + communityList.getName() + " is not supported");
                }
                for (StandardCommunity community : communityListLine.getCommunities()) {
                    communities.add(community.toString());
                }
            }
            Set<String> return_2 = new HashSet<>();
            return_2.add("standard");
            List<Set<String>> return_list = new ArrayList<>();
            return_list.add(communities);
            return_list.add(return_2);
            return return_list;

        }
        else {
            for (ExpandedCommunityListLine communityListLine : expandedCommunityList.getLines()) {
                if (communityListLine.getAction() != LineAction.PERMIT) {
                    throw new IllegalArgumentException("DENY in community list " + communityList.getName() + " is not supported");
                }
                communities.add(communityListLine.getRegex());
            }
            Set<String> return_2 = new HashSet<>();
            return_2.add("expanded");
            List<Set<String>> return_list = new ArrayList<>();
            return_list.add(communities);
            return_list.add(return_2);
            return return_list;
        }
    }

    public static Match convertCiscoMatch(CiscoConfiguration config, RouteMapMatchLine matchLine) {
        if (matchLine instanceof RouteMapMatchCommunityListLine line) {
            Set<String> tags = new HashSet<>();
            String type = "";
            for (String listName : line.getListNames()) {
                Set<String> communities = convertCiscoCommunityList(config, listName).get(0);
                tags.addAll(communities);
                if(convertCiscoCommunityList(config, listName).get(1).contains("standard")){
                    type = "standard";
                }
                else{
                    type = "expanded";
                }
            }
            if (type.equals("standard")){
                return new MatchCommunity(new NormalCommunityList(tags));
            }
            else{
                return new MatchCommunity(new RegexCommunityList(tags));
            }

        }else if (matchLine instanceof RouteMapMatchIpPrefixListLine line) {
            Set<Prefix> prefix = new HashSet<>();

            for (String listName : line.getListNames()) {
                org.batfish.representation.cisco.PrefixList prefixList = config.getPrefixLists().get(listName);
                if (prefixList == null) {
                    throw new IllegalArgumentException("can't find prefix list " + listName);
                }

                for (PrefixListLine prefixListLine : prefixList.getLines().values()) {
                    if (prefixListLine.getAction() != LineAction.PERMIT) {
                        throw new IllegalArgumentException("DENY in prefix list " + prefixList.getName() + " is not supported");
                    }
                    if (prefixListLine.getLengthRange().getStart() != prefixListLine.getPrefix().getPrefixLength()
                            || prefixListLine.getLengthRange().getEnd() != prefixListLine.getPrefix().getPrefixLength()) {
                        warn("only exact prefix match supported, sub range %s in %s will be ignored",
                            prefixListLine.getLengthRange(), prefixList.getName());
                    }
                    prefix.add(prefixListLine.getPrefix());
                }
            }

            return new MatchPrefix(prefix);
        } else {
            throw new IllegalArgumentException("unsupported Cisco match line " + matchLine.getClass());
        }
    }

    public static Setter convertCiscoSetter(CiscoConfiguration config, RouteMapSetLine setLine) {
        if (setLine instanceof RouteMapSetCommunityLine line) {
            Set<String> tags = line.getCommunities()
                    .stream().map(tag -> StandardCommunity.of(tag).toString()).collect(Collectors.toSet());
            return new SetCommunity(new NormalCommunityList(tags));
        } else if (setLine instanceof RouteMapSetAdditiveCommunityLine line) {
            Set<String> tags = line.getCommunities()
                    .stream().map(StandardCommunity::toString).collect(Collectors.toSet());
            return new AddCommunity(new NormalCommunityList(tags));
        } else if (setLine instanceof RouteMapSetDeleteCommunityLine line) {
            Set<String> tags = convertCiscoCommunityList(config, line.getListName()).get(0);
            return new DeleteCommunity(new NormalCommunityList(tags));
        } else if (setLine instanceof RouteMapSetLocalPreferenceLine line) {
            LongExpr expr = line.getLocalPreference();
            if (expr instanceof LiteralLong literal) {
                return new SetLocalPreference(literal.getValue());
            } else {
                throw new IllegalArgumentException("non-literal local preference " + expr.getClass() + " is not supported");
            }
        } else {
            throw new IllegalArgumentException("unsupported Cisco set line " + setLine.getClass());
        }
    }

    public static Clause convertCiscoClause(CiscoConfiguration config, RouteMapClause clause) {
        List<Match> matchList = clause.getMatchList()
                .stream()
                .map(line -> convertCiscoMatch(config, line))
                .toList();
        List<Setter> setterList = clause.getSetList()
                .stream()
                .map(line -> convertCiscoSetter(config, line))
                .toList();
        boolean permit = clause.getAction() == LineAction.PERMIT;
        Action action;
        if(permit){
            action = new PermitAction();
        }
        else{
            action = new DenyAction();
        }
        return new Clause(matchList, setterList, action);
    }

    public static RouteMap convertCiscoRouteMap(CiscoConfiguration config, org.batfish.representation.cisco.RouteMap rm) {
            List<Clause> clauses = rm.getClauses().navigableKeySet()
                    .stream()
                    .map(key -> convertCiscoClause(config, rm.getClauses().get(key)))
                    .toList();
        return new RouteMap(clauses);
    }

    private static CommunityList convertJuniperCommunity(JuniperConfiguration config, String communityName) {
        NamedCommunity community = config.getMasterLogicalSystem().getNamedCommunities().get(communityName);
        if (community == null) {
            throw new IllegalArgumentException("Can't find named community " + communityName);
        }

        Set<String> communities = new HashSet<>();
        for (CommunityMember communityMember : community.getMembers()) {
            if (communityMember instanceof LiteralCommunityMember literalCommunityMember) {
                communities.add(literalCommunityMember.getCommunity().toString());
                return new NormalCommunityList(communities);
            } else if(communityMember instanceof RegexCommunityMember regexCommunityMember) {
                communities.add(regexCommunityMember.getRegex());
                return new RegexCommunityList(communities);
//                warn("regex community member %s in %s is not supported ", communityMember, community.getName());
            }
        }
        return new NormalCommunityList(communities);
    }

    private static Set<Prefix> convertJuniperPrefixList(JuniperConfiguration config, String listName) {
        PrefixList prefixList = config.getMasterLogicalSystem().getPrefixLists().get(listName);
        if (prefixList == null) {
            throw new IllegalArgumentException("Can't find prefix list " + listName);
        }
        return prefixList.getPrefixes();
    }

    public static List<Match> convertJuniperMatch(JuniperConfiguration config, PsFroms froms) {
        List<Match> matchList = new ArrayList<>();
        for (PsFromCommunity fromCommunity : froms.getFromCommunities()) {
            CommunityList tags = convertJuniperCommunity(config, fromCommunity.getName());
            matchList.add(new MatchCommunity(tags));
        }

        for (PsFromPrefixList fromPrefixList : froms.getFromPrefixLists()) {
            Set<Prefix> prefixes = convertJuniperPrefixList(config, fromPrefixList.getName());
            matchList.add(new MatchPrefix(prefixes));
        }

        if (!froms.getFromPrefixListFilterLongers().isEmpty() || !froms.getFromPrefixListFilterOrLongers().isEmpty()) {
            for (PsFromPrefixListFilterLonger fromPrefixList : froms.getFromPrefixListFilterLongers()) {
                Set<Prefix> prefixes = convertJuniperPrefixList(config, fromPrefixList.getPrefixList());
                matchList.add(new MatchPrefix(prefixes));
            }
            for (PsFromPrefixListFilterOrLonger fromPrefixList : froms.getFromPrefixListFilterOrLongers()) {
                Set<Prefix> prefixes = convertJuniperPrefixList(config, fromPrefixList.getPrefixList());
                matchList.add(new MatchPrefix(prefixes));
            }
            warn("longer/orlonger in from prefix list filter will be ignored");
        }

        if (!froms.getFromRouteFilters().isEmpty()) {
            matchList.add(new MatchNothing());
            warn("from route filter will be ignored");
        }
        if (!froms.getFromProtocols().isEmpty()) {
            matchList.add(new MatchNothing());
            warn("from protocol will be ignored");
        }
        if (froms.getFromFamily() != null) {
            matchList.add(new MatchNothing());
            warn("from family will be ignored");
        }
        if (!froms.getFromAsPaths().isEmpty()) {
            matchList.add(new MatchNothing());
            warn("from as path will be ignored");
        }
        if (!froms.getFromInterfaces().isEmpty()) {
            matchList.add(new MatchNothing());
            warn("from interface will be ignored");
        }
        if (!froms.getFromTags().isEmpty()) {
            matchList.add(new MatchNothing());
            warn("from tags will be ignored");
        }

        if (!froms.getFromAsPathGroups().isEmpty() || froms.getFromColor() != null || froms.getFromCommunityCount() != null
                || !froms.getFromConditions().isEmpty() || froms.getFromInstance() != null || froms.getFromLocalPreference() != null
                || froms.getFromMetric() != null || !froms.getFromPolicyStatements().isEmpty() || !froms.getFromPolicyStatementConjunctions().isEmpty()) {
            throw new IllegalArgumentException("unsupported clauses in from");
        }

        return matchList;
    }

    @Nullable public static Setter convertJuniperSetter(JuniperConfiguration config, PsThen psThen) {
        if (psThen instanceof PsThenCommunityAdd then) {
            CommunityList communities = convertJuniperCommunity(config, then.getName());
            return new AddCommunity(communities);
        } else if (psThen instanceof PsThenCommunitySet then) {
            CommunityList communities = convertJuniperCommunity(config, then.getName());
            return new SetCommunity(communities);
        } else if (psThen instanceof PsThenCommunityDelete then) {
            CommunityList communities = convertJuniperCommunity(config, then.getName());
            return new DeleteCommunity(communities);
        } else if (psThen instanceof PsThenLocalPreference then) {
            return new SetLocalPreference(then.getLocalPreference());
//        } else if (psThen instanceof PsThenNextHopIp then) {
//            LOGGER.warn("then next hop ip is not supported and will be ignored");
//            return Optional.empty();
        } else {
            warn("%s is not supported and will be ignored", psThen.getClass());
//            throw new IllegalArgumentException("unsupported Juniper then " + psThen.getClass());
            return null;
        }
    }

    public static Clause convertJuniperClause(JuniperConfiguration config, PsTerm term) {
        List<Match> matchList = convertJuniperMatch(config, term.getFroms());
        Action action = new NextClauseAction();
        List<Setter> setterList = new ArrayList<>();
        for (PsThen then: term.getThens().getAllThens()) {
            if(then instanceof PsThenAccept) {
                action = new PermitAction();
            }
            else if(then instanceof PsThenReject) {
                action = new DenyAction();
            }
            else if(then instanceof PsThenNextPolicy){
                action = new NextPolicyAction();
            }
            else{
                Setter setter = convertJuniperSetter(config, then);
                if (setter != null) {
                    setterList.add(setter);
                }
            }
        }
        return new Clause(matchList, setterList, action);
    }

    public static RouteMap convertJuniperRouteMap(JuniperConfiguration config, PolicyStatement ps) {
        List<Clause> clauses = ps.getTerms().values().stream().map(term -> convertJuniperClause(config, term)).collect(Collectors.toList());
        boolean hasDefaultTerm = ps.getDefaultTerm().hasAtLeastOneFrom() || !ps.getDefaultTerm().getThens().isEmpty();
        if (hasDefaultTerm) {
            clauses.add(convertJuniperClause(config, ps.getDefaultTerm()));
        }
        return new RouteMap(clauses);
    }
}
