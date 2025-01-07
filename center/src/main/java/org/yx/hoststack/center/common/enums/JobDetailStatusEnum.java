package org.yx.hoststack.center.common.enums;

/**
 * @author Lee666
 * @Date 2025/1/3
 */

public enum JobDetailStatusEnum {
    WAIT("wait"),
    PROCESSING("processing"),
    FAIL("fail"),
    SUCCESS("success");

    private final String name;

    JobDetailStatusEnum(String name) {
        this.name= name;
    }

    public String getName() {
        return name;
    }

    public static JobDetailStatusEnum fromString(String name) {
        for (JobDetailStatusEnum mode : JobDetailStatusEnum.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown register mode: " + name);
    }
}
