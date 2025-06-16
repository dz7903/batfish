package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.Set;

public class RegexCommunityList extends CommunityList implements Serializable {
    public final String type = "regex_community_list";
    public Set<String> names;

    public RegexCommunityList(Set<String> names) {
        this.names = names;
    }
}
