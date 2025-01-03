package org.yx.hoststack.edge.cache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegionCoturnConfig {
    private String serverSvc;
    private String serverUser;
    private String serverPwd;
}
