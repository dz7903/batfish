package org.batfish.question.vendorspecific.ir;

import org.batfish.datamodel.Ip;

import javax.annotation.Nullable;
import java.io.Serializable;

public class Interface implements Serializable {
    public final Ip localIp;
    public final Long localAs;
    public final Ip remoteIp;
    @Nullable public final Long remoteAs;
    public final boolean isInternal;
    @Nullable public final RouteMap importRouteMap;
    @Nullable public final RouteMap exportRouteMap;

    public Interface(Ip localIp, Long localAs, Ip remoteIp, Long remoteAs, boolean isInternal, RouteMap importRouteMap, RouteMap exportRouteMap) {
        this.localIp = localIp;
        this.localAs = localAs;
        this.remoteIp = remoteIp;
        this.isInternal = isInternal;
        this.remoteAs = remoteAs;
        this.importRouteMap = importRouteMap;
        this.exportRouteMap = exportRouteMap;
    }
}
