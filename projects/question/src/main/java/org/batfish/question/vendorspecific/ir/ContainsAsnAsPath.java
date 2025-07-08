package org.batfish.question.vendorspecific.ir;

import com.google.common.collect.Range;

import java.io.Serializable;
import java.util.List;

public class ContainsAsnAsPath extends MyAsPath implements Serializable {
    public final String type = "contains_asn_as_path";
    public final List<Range<Long>> ranges;

    public ContainsAsnAsPath(List<Range<Long>> ranges) {
        this.ranges = ranges;
    }
}
