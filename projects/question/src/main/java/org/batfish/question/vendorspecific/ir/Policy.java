package org.batfish.question.vendorspecific.ir;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

public class Policy implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL) public final List<Clause> clauses;

    public Policy(List<Clause> clauses) {
        this.clauses = clauses;
    }
}
