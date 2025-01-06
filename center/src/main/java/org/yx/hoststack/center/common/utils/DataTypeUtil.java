package org.yx.hoststack.center.common.utils;

/**
 * Data type conversion utility class
 *
 * @author riyouchou
 * @since 2025-01-04
 */
public class DataTypeUtil {
    
    private DataTypeUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert Long to uint32 (as int in Java)
     * Handle null, negative values and overflow cases
     *
     * @param value Long value to convert
     * @return int value in uint32 range (0 to 4294967295)
     */
    public static int convertToUInt32(Long value) {
        if (value == null) {
            return 0;
        }
        if (value < 0) {
            return 0;
        }
        if (value > 0xFFFFFFFFL) {
            return 0xFFFFFFFF;  // Return max value of uint32
        }
        return value.intValue();
    }

    /**
     * Check if value is within uint32 range
     *
     * @param value value to check
     * @return true if value is within uint32 range
     */
    public static boolean isValidUInt32(Long value) {
        return value != null && value >= 0 && value <= 0xFFFFFFFFL;
    }
}