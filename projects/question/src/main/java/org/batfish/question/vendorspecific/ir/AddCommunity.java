package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.Set;

public class AddCommunity extends Setter implements Serializable {
    public final String type = "add_community";

    public final Set<String> communities;

    public AddCommunity(Set<String> communities) {
        this.communities = communities;
    }
}