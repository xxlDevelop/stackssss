package org.yx.hoststack.center.common.enums;

/**
 * @author Lee666
 * @Date 2025/1/3
 */

public enum VolumeTypeEnum {
    BASE_VOLUME("base"),
    USER_VOLUME("user");

    private final String name;

    VolumeTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static VolumeTypeEnum fromString(String name) {
        for (VolumeTypeEnum mode : VolumeTypeEnum.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown register mode: " + name);
    }
}
