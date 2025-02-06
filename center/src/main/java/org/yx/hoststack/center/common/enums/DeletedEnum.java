package org.yx.hoststack.center.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Delete status enum
 *
 * @author lyc
 * @since 2025-02-05
 */
@Getter
@AllArgsConstructor
public enum DeletedEnum {

    /**
     * Not deleted status
     */
    NOT_DELETED(0, "Not Deleted"),

    /**
     * Deleted status
     */
    DELETED(1, "Deleted");

    /**
     * Status code
     */
    @EnumValue
    @JsonValue
    private final Integer code;

    /**
     * Status description
     */
    private final String description;

    /**
     * Get enum by code
     *
     * @param code status code
     * @return corresponding enum value or null if not found
     */
    public static DeletedEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeletedEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if the status is deleted
     *
     * @return true if deleted, false otherwise
     */
    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * Check if the status is not deleted
     *
     * @return true if not deleted, false otherwise
     */
    public boolean isNotDeleted() {
        return this == NOT_DELETED;
    }
}