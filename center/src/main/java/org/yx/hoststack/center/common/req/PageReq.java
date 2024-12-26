package org.yx.hoststack.center.common.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageReq {
    private Long current = 1L;
    private Long size = 20L;
}
