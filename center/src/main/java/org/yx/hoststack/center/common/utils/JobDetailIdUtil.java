package org.yx.hoststack.center.common.utils;


import static org.yx.hoststack.center.common.constant.JobDetailConstants.JOB_DETAIL_ID_FORMAT;

/**
 * @Description : Container ID VO handle util
 * @Author : Lee666
 * @Date : 2025/1/4
 * @Version : 1.0
 */
public class JobDetailIdUtil {

    private JobDetailIdUtil() {
    }

    /**
     * get JobDetail id
     * @param jobId job id
     * @param targetId target id(hostId or containerId or volumeId or ....)
     * @return id
     */
    public static String getContainerId(String jobId, String targetId) {
        return String.format(JOB_DETAIL_ID_FORMAT, jobId, targetId);
    }
}
