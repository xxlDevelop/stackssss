package org.yx.hoststack.edge.common;

public interface CacheKeyConstants {
    String keyPrefix = "host_stack:edge:";
    String IdcId = keyPrefix + "idc_id";
    String IdcBasicConfig = keyPrefix + "basic_config_%s";
    String IdcNetConfig = keyPrefix + "net_config_%s";
    String OssConfig = keyPrefix + "oss_config";
    String CoturnConfig = keyPrefix + "coturn_config";
    String Job = keyPrefix + "job:id_%s";
    String JobDetail = keyPrefix + "job:sub_%s";
}
