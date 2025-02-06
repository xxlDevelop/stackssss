package org.yx.hoststack.center.common.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageReq {
    /**
     * Current page index, default is 1
     */
    private Long current = 1L;
    /**
     * Page size, default is 20
     */
    private Long size = 20L;
}
