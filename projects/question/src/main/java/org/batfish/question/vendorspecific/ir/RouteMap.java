package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RouteMap implements Serializable {
    public final List<Clause> clauses;

    public RouteMap(List<Clause> clauses) {
        this.clauses = clauses;
    }

    public RouteMap() {
        this.clauses = new ArrayList<>();
    }

    public void merge(RouteMap other) {
        clauses.addAll(other.clauses);
    }
}
