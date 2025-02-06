package org.yx.hoststack.center.common.enums;

public enum DistributeStatusEnum {
    INITIALIZED(0, "Initialized"),
    DISTRIBUTING(1, "Distributing"),
    DISTRIBUTED_SUCCESSFULLY(2, "Distributed Successfully"),
    DISTRIBUTION_FAILED(3, "Distribution Failed");

    private final int code;
    private final String description;

    DistributeStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // Optionally, you can add a method to get the enum by its code
    public static DistributeStatusEnum fromCode(int code) {
        for (DistributeStatusEnum status : DistributeStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching distribute status for code: " + code);
    }
}