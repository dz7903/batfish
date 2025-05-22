package org.batfish.question.vendorspecific.ir;

import org.batfish.datamodel.Prefix;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Set;

public class MatchPrefix extends Match implements Serializable {
    public final String type = "match_prefix";
    public final @Nonnull Set<Prefix> prefixes;

    public MatchPrefix(Set<Prefix> prefixes) {
        this.prefixes = prefixes;
    }
}
