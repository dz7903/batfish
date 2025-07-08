package org.batfish.question.vendorspecific.ir;

import com.google.common.collect.Range;

import java.io.Serializable;
import java.util.List;

public class ContainsAsnRangeAsPath extends MyAsPath implements Serializable {
    public final String type = "contains_asn_range_as_path";
    public final List<Range<Long>> ranges;

    public ContainsAsnRangeAsPath(List<Range<Long>> ranges) {
        this.ranges = ranges;
    }
}
