package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;

public class DenyAction extends Action implements Serializable {
    public final String type = "deny";

    public DenyAction() {}
}
