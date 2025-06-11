package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Clause implements Serializable {

    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<Match> matchList;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<Setter> setterList;
    public boolean permit;

    public Clause(List<Match> matchList, List<Setter> setterList, boolean permit) {
        this.matchList = matchList;
        this.setterList = setterList;
        this.permit = permit;
    }
}
