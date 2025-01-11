package org.yx.remote;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.yx.lib.utils.util.R;
import org.yx.remote.config.UserCenterApiConfig;
import org.yx.remote.model.resp.TenantInfoResp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * uc Center TenantInfo 相关服务
 */
public class UserCenterRemoteTenantInfoService extends UserCenterCenterRemoteService {

    public UserCenterRemoteTenantInfoService(RestTemplate restTemplate, UserCenterApiConfig ucCenterApiConfig) {
        super(restTemplate, ucCenterApiConfig);
    }


    /**
     * 鉴权token合法性
     *
     * @return
     */
    public List<TenantInfoResp> authCheckToken(String token) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(H_X_TOKEN, token);

        ResponseEntity<String> responseEntity = postRoute(ucCenterApiConfig.getAuthCheckTokenUrl(), new HashMap<>(), headers);
        if (responseEntity != null) {
            R<List<TenantInfoResp>> r = JSON.parseObject(responseEntity.getBody(), new TypeReference<R<TenantInfoResp>>() {
            }.getType());
            if (r != null && r.getCode() == 0) {
                return r.getData();
            }
        }
        return null;
    }
}
