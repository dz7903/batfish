package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;

public class PermitAction extends Action implements Serializable {
    public final String type = "permit";

    public PermitAction() {}
}
