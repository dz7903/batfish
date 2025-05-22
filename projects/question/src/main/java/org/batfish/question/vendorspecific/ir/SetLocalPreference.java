package org.batfish.question.vendorspecific.ir;

import java.io.Serializable;

public class SetLocalPreference extends Setter implements Serializable {
    public final String type = "set_local_preference";
    public final long lp;

    public SetLocalPreference(long lp) {
        this.lp = lp;
    }
}
