package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.List;
//import java.util.Set;

public class DeleteCommunity extends Setter implements Serializable {
    public final String type = "delete_community";
    public final List<CommunityList> communityLists;

    public DeleteCommunity(List<CommunityList> communityLists) {
        this.communityLists = communityLists;
    }
}
