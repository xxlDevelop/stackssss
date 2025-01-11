package org.yx.remote.model.resp;

import lombok.Data;

/**
 *
 * packageName org.yx.remote.model.resp
 * @author YI-JIAN-ZHANG
 * @version JDK 8
 * @className UserInfoResp
 * @date 2025/1/10
 */
@Data
public class TenantInfoResp {

    private Long tid;

    private String sk;

    private String name;

    private String email;
}
