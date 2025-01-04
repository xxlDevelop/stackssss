package org.yx.hoststack.center.common.enums;

/**
 * @author Lee666
 * @Date 2025/1/3
 */

public enum JobTypeEnum {
    HOST("host"),
    VOLUME("volume"),
    CONTAINER("container"),
    FILE("file"),
    MODULE("module");

    private final String name;

    JobTypeEnum(String name) {
        this.name= name;
    }

    public String getName() {
        return name;
    }

    public static JobTypeEnum fromString(String name) {
        for (JobTypeEnum mode : JobTypeEnum.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown register mode: " + name);
    }
}
