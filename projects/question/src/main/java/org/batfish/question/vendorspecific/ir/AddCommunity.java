package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.List;

public class AddCommunity extends Setter implements Serializable {
    public final String type = "add_community";
    public final List<CommunityList> communityLists;

    public AddCommunity(List<CommunityList> communityLists) {
        this.communityLists = communityLists;
    }
}