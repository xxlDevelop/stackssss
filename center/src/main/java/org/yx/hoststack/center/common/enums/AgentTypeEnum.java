package org.yx.hoststack.center.common.enums;

/**
 * @author Lee666
 * @Date 2025/1/3
 */

public enum AgentTypeEnum {
    HOST("host"),
    CONTAINER("container");

    private final String name;

    AgentTypeEnum(String name) {
        this.name= name;
    }

    public String getName() {
        return name;
    }

    public static AgentTypeEnum fromString(String name) {
        for (AgentTypeEnum mode : AgentTypeEnum.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown register mode: " + name);
    }
}
