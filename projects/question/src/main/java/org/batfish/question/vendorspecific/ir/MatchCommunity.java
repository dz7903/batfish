package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.List;
//import java.util.Set;

public class MatchCommunity extends Match implements Serializable {
    public final String type = "match_community";
    public final List<CommunityList> communityLists;

    public MatchCommunity(List<CommunityList> communityLists) {
        this.communityLists = communityLists;
    }
}