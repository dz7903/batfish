package org.batfish.question.vendorspecific.ir;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

public class SetCommunity extends Setter implements Serializable {
    public final String type = "set_community";
    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<CommunityList> communityLists;

    public SetCommunity(List<CommunityList> communityLists) {
        this.communityLists = communityLists;
    }
}
