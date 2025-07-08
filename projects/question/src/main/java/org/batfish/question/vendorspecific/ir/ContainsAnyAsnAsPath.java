package org.batfish.question.vendorspecific.ir;

import com.google.common.collect.Range;

import java.io.Serializable;
import java.util.List;

public class ContainsAnyAsnAsPath extends MyAsPath implements Serializable {
    public final String type = "contains_any_asn_as_path";
    public final List<List<Range<Long>>>  ranges;

    public ContainsAnyAsnAsPath(List<List<Range<Long>>> ranges) {
        this.ranges = ranges;
    }
}
