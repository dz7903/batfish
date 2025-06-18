package org.batfish.question.vendorspecific.ir;

import org.batfish.datamodel.Prefix;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Set;

public class MatchPrefix extends Match implements Serializable {
    public final String type = "match_prefix";
    @JsonInclude(JsonInclude.Include.NON_NULL) public final Set<Prefix> prefixes;

    public MatchPrefix(Set<Prefix> prefixes) {
        this.prefixes = prefixes;
    }
}
