package org.batfish.question.vendorspecific.ir;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Set;

public class RegexCommunityList extends CommunityList implements Serializable {
    public final String type = "regex_community_list";
    @JsonInclude(JsonInclude.Include.NON_NULL) public Set<String> names;

    public RegexCommunityList(Set<String> names) {
        this.names = names;
    }
}
