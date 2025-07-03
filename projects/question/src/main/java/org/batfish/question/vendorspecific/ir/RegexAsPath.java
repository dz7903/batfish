package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.Set;

public class RegexAsPath extends MyAsPath implements Serializable {
    public final String type = "regex_as_path";
    public final Set<String> names;

    public RegexAsPath(Set<String> names) {
        this.names = names;
    }
}
