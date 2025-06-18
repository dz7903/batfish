package org.batfish.question.vendorspecific.ir;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nonnull;
import java.util.List;

public class PolicySet {
    @JsonInclude(JsonInclude.Include.NON_NULL) @Nonnull public List<Policy> policies;
    public final Action defaultAction;

    public PolicySet(@Nonnull List<Policy> policies, Action defaultAction) {
        this.policies = policies;
        this.defaultAction = defaultAction;
    }
}
