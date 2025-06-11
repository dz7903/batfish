package org.batfish.question.vendorspecific.ir;

import org.batfish.datamodel.Ip;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Interface implements Serializable {
    public final Ip localIp;
    public final Long localAs;
    public final Ip remoteIp;
    @Nullable public final Long remoteAs;
    public final boolean isInternal;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final ArrayList<RouteMap> importRouteMaps;
    @JsonInclude(JsonInclude.Include.NON_NULL) public final ArrayList<RouteMap> exportRouteMaps;

    public Interface(Ip localIp, Long localAs, Ip remoteIp, @Nullable Long remoteAs, boolean isInternal, ArrayList<RouteMap> importRouteMaps, ArrayList<RouteMap> exportRouteMaps) {
        this.localIp = localIp;
        this.localAs = localAs;
        this.remoteIp = remoteIp;
        this.isInternal = isInternal;
        this.remoteAs = remoteAs;
        this.importRouteMaps = importRouteMaps;
        this.exportRouteMaps = exportRouteMaps;
    }
}
