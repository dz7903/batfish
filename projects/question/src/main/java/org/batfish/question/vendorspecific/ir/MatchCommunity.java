package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
//import java.util.Set;

public class MatchCommunity extends Match implements Serializable {
    public final String type = "match_community";
    public final CommunityList communities;

    public MatchCommunity(CommunityList communities) {
        this.communities = communities;
    }
}