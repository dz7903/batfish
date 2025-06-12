package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;

public class NextPolicyAction extends Action implements Serializable {
    public final String type = "next_policy";

    public NextPolicyAction() {}
}
