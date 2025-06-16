package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
//import java.util.Set;

public class SetCommunity extends Setter implements Serializable {
    public final String type = "set_community";

    public final CommunityList communities;

    public SetCommunity(CommunityList communities) {
        this.communities = communities;
    }
}
