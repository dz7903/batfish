package org.batfish.question.vendorspecific.ir;

import org.batfish.datamodel.Ip;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

public class Interface implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL) public final Ip localIp;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final Long localAs;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final Ip remoteIp;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final Long remoteAs;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final Boolean isInternal;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final PolicySet importPolicies;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final PolicySet exportPolicies;

    public Interface(
            Ip localIp, Long localAs, Ip remoteIp, Long remoteAs, Boolean isInternal,
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
