package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;

public class NextClauseAction extends Action implements Serializable {
    public final String type = "next_clause";

    public NextClauseAction() {}
}
