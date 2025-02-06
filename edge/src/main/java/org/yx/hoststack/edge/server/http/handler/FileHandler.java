package org.yx.hoststack.edge.server.http.handler;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class FileHandler {

    private final MessageQueues messageQueues;

    public Mono<ServerResponse> distributeNotify(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request.bodyToMono(String.class)
                                .flatMap(bodyStr -> {
                                    String imageId = request.queryParam("imageId").orElse("");
                                    String imageVer = request.queryParam("imageVer").orElse("");
                                    String traceId = request.headers().firstHeader("traceId");
                                    JSONObject body = JSON.parseObject(bodyStr);
                                    String jobId = body.getString("jobId");
                                    String status = body.getString("status");
                                    int code = body.getIntValue("code");
                                    String msg = body.getString("msg");
                                    String md5 = body.getString("md5");
                                    String idcStoragePath = body.getString("idcStoragePath");

                                    KvLogger.instance(this)
                                            .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.IMAGE_DISTRIBUTE_CALLBACK)
                                            .p(LogFieldConstants.TRACE_ID, traceId)
                                            .p(LogFieldConstants.Code, code)
                                            .p(LogFieldConstants.ERR_MSG, msg)
                                            .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                            .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                            .p(HostStackConstants.REGION, EdgeContext.Region)
                                            .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                            .p(HostStackConstants.JOB_ID, jobId)
                                            .p("ImageId", imageId)
                                            .p("Status", status)
                                            .i();
                                    AgentCommonMessage<JSONObject> jobReport = AgentCommonMessage.<JSONObject>builder()
                                            .jobId(jobId)
                                            .status(status)
                                            .progress(100)
                                            .code(code)
                                            .msg(msg)
                                            .traceId(traceId)
                                            .data(new JSONObject()
                                                    .fluentPut("imageId", imageId)
                                                    .fluentPut("imageVer", imageVer)
                                                    .fluentPut("idcStoragePath", idcStoragePath)
                                                    .fluentPut("netStoragePath", "")
                                                    .fluentPut("md5", md5))
                                            .build();
                                    messageQueues.getJobNotifyToCenterQueue().add(jobReport);
                                    return Mono.just(R.ok());
                                })
                        , R.class);
    }
}
