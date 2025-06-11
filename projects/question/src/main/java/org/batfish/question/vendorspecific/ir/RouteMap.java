package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

public class RouteMap implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<Clause> clauses;

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
