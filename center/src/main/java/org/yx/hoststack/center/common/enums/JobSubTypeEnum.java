package org.yx.hoststack.center.common.enums;

/**
 * @author Lee666
 * @Date 2025/1/3
 */

public enum JobSubTypeEnum {
    PARENT_FOR_CREATE_CONTAINER("createContainer"),
    CREATE("create"),
    UPDATE("update"),
    UPDATE_CONFIG("update_config"),
    DELETE("delete"),
    RESET("reset"),
    UPGRADE("upgrade"),
    CONFIG("config"),
    MOUNT("mount"),
    UNMOUNT("unmount"),
    UPDATE_PROFILE("update_profile"),
    START("start"),
    SHUTDOWN("shutdown"),
    REBOOT("reboot"),
    DROP("drop"),
    EXEC_CMD("exec_cmd"),
    INSTALL("install"),
    UNINSTALL("uninstall");

    private final String name;

    JobSubTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static JobSubTypeEnum fromString(String name) {
        for (JobSubTypeEnum mode : JobSubTypeEnum.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown register mode: " + name);
    }
}
