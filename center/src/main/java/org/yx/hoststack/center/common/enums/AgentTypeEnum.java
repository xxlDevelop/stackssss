package org.yx.hoststack.center.common.enums;

/**
 * @author Lee666
 * @Date 2025/1/3
 */

public enum AgentTypeEnum {
    HOST("host"),
    CONTAINER("container"),
    BENCHMARK("benchmark");

    private String name;


    AgentTypeEnum(String name) {
        this.name= name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static AgentTypeEnum fromString(String name) {
        for (AgentTypeEnum mode : AgentTypeEnum.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown agent type: " + name);
    }
}
