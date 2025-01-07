package org.yx.hoststack.center.common.utils;

import java.util.UUID;

/**
 * @Description :
 * @Author : Lee666
 * @Date : 2025/1/4
 * @Version : 1.0
 */
public class UUIDUtils {

    /**
     * 简化的UUID，去掉了横线
     * @return UUID
     */
    public static String fastSimpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
