package org.yx.hoststack.protocol.ws.agent.common;

import lombok.Getter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

// http://192.168.31.17:32080/pages/viewpage.action?pageId=3168224
@Getter
public enum AgentMethodId {
    InitializeMachine("InitializeMachine"),
    HeartBeat("HeartBeat"),
    UpdateConfig("UpdateConfig"),
    QueryConfig("QueryConfig"),
    ControlHost("ControlHost"),
    ResetHost("ResetHost"),
    UpgradeImage("UpgradeImage"),
    CreateVM("CreateVM"),
    CreateAndStartVM("CreateAndStartVM"),
    ControlVM("ControlVM"),
    ResetVM("ResetVM"),
    QueryJobStatus("QueryJobStatus"),
    LightCapacity("LightCapacity"),
    JobNotify("JobNotify")
    ;

    private static final Map<String, AgentMethodId> COMMAND_MAP = new HashMap<>();

    static {
        for (AgentMethodId e : EnumSet.allOf(AgentMethodId.class)) {
            COMMAND_MAP.put(e.value, e);
        }
    }

    private final String value;

    AgentMethodId(String value) {
        this.value = value;
    }

    public static AgentMethodId find(String methodId) {
        return COMMAND_MAP.get(methodId);
    }
}
