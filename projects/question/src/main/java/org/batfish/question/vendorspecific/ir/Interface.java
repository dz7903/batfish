package org.batfish.question.vendorspecific.ir;

import org.batfish.datamodel.Ip;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;
import java.io.Serializable;

public class Interface implements Serializable {
    public final Ip localIp;
    public final Long localAs;
    public final Ip remoteIp;
    @Nullable public final Long remoteAs;
    public final boolean isInternal;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final PolicySet importPolicies;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final PolicySet exportPolicies;

    public Interface(
            Ip localIp, Long localAs, Ip remoteIp, @Nullable Long remoteAs, boolean isInternal,
            PolicySet importPolicies, PolicySet exportPolicies) {
        this.localIp = localIp;
        this.localAs = localAs;
        this.remoteIp = remoteIp;
        this.isInternal = isInternal;
        this.remoteAs = remoteAs;
        this.importPolicies = importPolicies;
        this.exportPolicies = exportPolicies;
    }
}
