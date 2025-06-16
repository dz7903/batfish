package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.Set;

public class NormalCommunityList extends CommunityList implements Serializable {
    public final String type = "normal_community_list";
    public Set<String> names;

    public NormalCommunityList(Set<String> names) {
        this.names = names;
    }
}
