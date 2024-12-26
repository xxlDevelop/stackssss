package org.yx.hoststack.center.common.enums;

public enum RegisterModeEnum {
    HOST("host"),
    CONTAINER("container"),
    BENCHMARK("benchmark");

    private String name;


    RegisterModeEnum(String name) {
        this.name= name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static RegisterModeEnum fromString(String name) {
        for (RegisterModeEnum mode : RegisterModeEnum.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown register mode: " + name);
    }
}
