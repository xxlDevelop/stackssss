package org.yx.hoststack.center.common.utils;

import static org.yx.hoststack.center.common.constant.ContainerConstants.CONTAINER_ID_FORMAT;

/**
 * @Description : Container ID VO handle util
 * @Author : Lee666
 * @Date : 2025/1/4
 * @Version : 1.0
 */
public class ContainerIdUtil {

    private ContainerIdUtil() {
    }

    public static String getContainerId(String hostId, int sequenceNumber) {
        return String.format(CONTAINER_ID_FORMAT, hostId, sequenceNumber);
    }
}
