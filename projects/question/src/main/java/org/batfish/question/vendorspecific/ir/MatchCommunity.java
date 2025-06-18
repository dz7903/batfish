package org.batfish.question.vendorspecific.ir;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

public class MatchCommunity extends Match implements Serializable {
    public final String type = "match_community";
    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<CommunityList> communityLists;

    public MatchCommunity(List<CommunityList> communityLists) {
        this.communityLists = communityLists;
    }
}