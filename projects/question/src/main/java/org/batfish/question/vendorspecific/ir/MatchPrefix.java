package org.batfish.question.vendorspecific.ir;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.batfish.datamodel.PrefixRange;

import java.io.Serializable;
import java.util.Set;

public class MatchPrefix extends Match implements Serializable {
    public final String type = "match_prefix";
    @JsonInclude(JsonInclude.Include.NON_NULL) public final Set<PrefixRange> prefixRanges;

    public MatchPrefix(Set<PrefixRange> prefixRanges) {
        this.prefixRanges = prefixRanges;
    }
}
