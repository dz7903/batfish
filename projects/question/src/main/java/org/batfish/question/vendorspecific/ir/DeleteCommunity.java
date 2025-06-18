package org.batfish.question.vendorspecific.ir;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

public class DeleteCommunity extends Setter implements Serializable {
    public final String type = "delete_community";
    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<CommunityList> communityLists;

    public DeleteCommunity(List<CommunityList> communityLists) {
        this.communityLists = communityLists;
    }
}
