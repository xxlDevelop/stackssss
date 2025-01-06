package org.yx.hoststack.protocol.ws.server;

import lombok.Getter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum ProtoMethodId {
    ForwardingToCenter(100),
    ForwardingToIdc(101),
    ForwardingFailed(105),
    EdgeRegister(10000),
    Ping(10001),
    Pong(10002),
    EdgeConfigSync(10003),
    RegionConfigSync(10004),
    IdcNetConfig(10005),
    IdcExit(10009),
    HostInitialize(10200),
    HostRest(10201),
    HostHeartbeat(10202),
    BmkHeartbeat(10203),
    HostExit(10204),
    GetHostConfig(10205),
    GetContainerConfig(12010),
    DoJob(15000),
    JobReport(15001)
    ;
    private static final Map<Integer, ProtoMethodId> COMMAND_MAP = new HashMap<>();

    static {
        for (ProtoMethodId e : EnumSet.allOf(ProtoMethodId.class)) {
            COMMAND_MAP.put(e.value, e);
        }
    }

    private final Integer value;

    ProtoMethodId(Integer value) {
        this.value = value;
    }

    public static ProtoMethodId find(int methodId) {
        return COMMAND_MAP.get(methodId);
    }
}
