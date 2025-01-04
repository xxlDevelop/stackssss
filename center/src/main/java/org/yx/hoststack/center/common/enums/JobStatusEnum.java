package org.yx.hoststack.center.common.enums;

/**
 * @author Lee666
 * @Date 2025/1/3
 */

public enum JobStatusEnum {
    WAIT("wait"),
    PROCESSING("processing"),
    FAIL("fail"),
    SUCCESS("success");

    private final String name;

    JobStatusEnum(String name) {
        this.name= name;
    }

    public String getName() {
        return name;
    }

    public static JobStatusEnum fromString(String name) {
        for (JobStatusEnum mode : JobStatusEnum.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown register mode: " + name);
    }
}
