package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
//import java.util.Set;

public class DeleteCommunity extends Setter implements Serializable {
    public final String type = "delete_community";
    public final CommunityList tags;

    public DeleteCommunity(CommunityList tags) {
        this.tags = tags;
    }
}
