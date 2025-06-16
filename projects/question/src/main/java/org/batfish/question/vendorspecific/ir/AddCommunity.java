package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
//import java.util.Set;

public class AddCommunity extends Setter implements Serializable {
    public final String type = "add_community";

    public final CommunityList communities;

    public AddCommunity(CommunityList communities) {
        this.communities = communities;
    }
}