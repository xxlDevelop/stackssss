package org.yx.hoststack.edge.cache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegionStorageConfig {
    private String type;
    private String config;
}
