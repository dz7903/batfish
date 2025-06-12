package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Clause implements Serializable {

    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<Match> matchList;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<Setter> setterList;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final Action action;

    public Clause(List<Match> matchList, List<Setter> setterList, Action action) {
        this.matchList = matchList;
        this.setterList = setterList;
        this.action = action;
    }
}
