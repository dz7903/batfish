package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.List;

public class Clause implements Serializable {

    public final List<Match> matchList;
    public final List<Setter> setterList;
    public boolean permit;

    public Clause(List<Match> matchList, List<Setter> setterList, boolean permit) {
        this.matchList = matchList;
        this.setterList = setterList;
        this.permit = permit;
    }
}
