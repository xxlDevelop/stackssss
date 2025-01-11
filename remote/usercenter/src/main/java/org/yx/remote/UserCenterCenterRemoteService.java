package org.yx.remote;

import com.google.common.base.Predicate;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.YxRetryUtil;
import org.yx.remote.config.UserCenterApiConfig;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserCenterCenterRemoteService {

    protected final RestTemplate restTemplate;
    protected final UserCenterApiConfig ucCenterApiConfig;

    public static final String H_X_TOKEN = "x-token";
    public static final String H_X_USER = "x-user";

    public UserCenterCenterRemoteService(RestTemplate restTemplate, UserCenterApiConfig ucCenterApiConfig) {
        this.restTemplate = restTemplate;
        this.ucCenterApiConfig = ucCenterApiConfig;
    }

    protected ResponseEntity<String> postRoute(String url, Object body, Map<String, String> headers) {
        return postRoute(url, body, null, headers, response -> response == null || response.getStatusCode().value() != HttpStatus.OK.value());
    }

    protected ResponseEntity<String> postRoute(String url, Object body, Map<String, Object> customerTokenPayload, Map<String, String> headers) {
        return postRoute(url, body, customerTokenPayload, headers, response -> response.getStatusCode().value() != HttpStatus.OK.value());
    }

    protected ResponseEntity<String> postRoute(String url, Object body, Map<String, Object> customerTokenPayload,
                                               Map<String, String> headers, Predicate<ResponseEntity<String>> predicate) {
        return postRoute(url, body, customerTokenPayload, headers, 3, 1, TimeUnit.SECONDS, predicate);
    }

    protected ResponseEntity<String> postRoute(String url, Object body, Map<String, Object> customerTokenPayload, Map<String, String> headers,
                                               int retryTimes, int retrySleepTime, TimeUnit retrySleepUnit, Predicate<ResponseEntity<String>> predicate) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (headers != null) {
            headers.keySet().forEach(headerName -> httpHeaders.add(headerName, headers.get(headerName)));
        }
        httpHeaders.add(CommonConstants.TRACE_ID_HEADER, MDC.get(CommonConstants.TRACE_ID));
        HttpEntity<?> entity = new HttpEntity<>(body, httpHeaders);
        try {
            return YxRetryUtil.retry(() -> restTemplate.postForEntity(url, entity, String.class),
                    retrySleepTime, retrySleepUnit, retryTimes, predicate);
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, "UcCenterRemoteCall")
                    .p(LogFieldConstants.ACTION, "PostRoute")
                    .p("ReqUrl", url)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .e(e);
            return null;
        }
    }

}
