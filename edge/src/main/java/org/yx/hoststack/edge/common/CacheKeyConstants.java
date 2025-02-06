package org.yx.hoststack.edge.common;

public interface CacheKeyConstants {
    String keyPrefix = "host_stack:edge:";
    String IdcBasicConfigObj = keyPrefix + "basic_config_obj_%s";
    String IdcBasicConfigMap = keyPrefix + "basic_config_map_%s";
    String IdcNetConfig = keyPrefix + "net_config_%s";
    String RegionStorageConfig = keyPrefix + "region_storage_config_%s";
    String RegionCoturnConfig = keyPrefix + "region_coturn_config_%s";
    String RegionJob = keyPrefix + "job:%s";
}
