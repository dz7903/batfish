package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;

public class MatchAsPath extends Match implements Serializable {
    public final String type = "match_as_path";
    public final MyAsPath asPath;

    public MatchAsPath(MyAsPath asPath) {
        this.asPath = asPath;
    }
}
