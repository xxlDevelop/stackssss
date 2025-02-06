package org.yx.hoststack.center.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.WebClient;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.JobTypeEnum;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.jobs.ImageJob;
import org.yx.hoststack.center.jobs.JobManager;
import org.yx.hoststack.center.jobs.SendJobResult;
import org.yx.hoststack.center.jobs.cmd.JobCmd;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.image.CreateImageCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.CreateVolumeCmdData;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    private final WebClient webClient;

    public TestController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/aaa")
    public R<String> test() {
//        JobInnerCmd<CreateImageCmdData> jobInnerCmd = SpringContextHolder.getBean(JobManager.class).createJob(
//                JobCmd.<CreateImageCmdData>builder()
//                        .tenantId(10000L)
//                        .jobType(JobTypeEnum.Image)
//                        .jobSubType(JobSubTypeEnum.CREATE)
//                        .jobData(CreateImageCmdData.builder()
//                                .imageId("imageId")
//                                .imageName("imageName")
//                                .imageVer("1.1")
//                                .downloadUrl("download")
//                                .md5("md5")
//                                .idcInfos(Lists.newArrayList(CreateImageCmdData.IdcInfo.builder()
//                                        .idc("China-North-BeiJing-IDC-000009")
//                                        .bucket("bucket")
//                                        .build()))
//                                .build())
//                        .build()
//        );
        JobInnerCmd<CreateVolumeCmdData> jobInnerCmd = SpringContextHolder.getBean(JobManager.class).createJob(
                JobCmd.<CreateVolumeCmdData>builder()
                        .tenantId(10000L)
                        .jobType(JobTypeEnum.VOLUME)
                        .jobSubType(JobSubTypeEnum.CREATE)
                        .jobData(CreateVolumeCmdData.builder()
                                .volumeSize(100)
                                .volumeType("user")
                                .diskType("local")
                                .hostId("hostId01")
                                .volumeId(Lists.newArrayList("volumeId01", "volumeId03", "volumeId02"))
                                .build())
                        .build()
        );
        JobInfo jobInfo = SpringContextHolder.getBean(JobInfoService.class).getOne(Wrappers.lambdaQuery(JobInfo.class)
                .eq(JobInfo::getJobId, jobInnerCmd.getJobId()));
        SpringContextHolder.getBean(JobManager.class).sendJob(JSON.parseObject(jobInfo.getJobInnerCmd(), new TypeReference<JobInnerCmd<?>>() {
        }.getType()));
        R<SendJobResult> sendJobResultR = SpringContextHolder.getBean(JobManager.class).sendJob(jobInnerCmd);
        SpringContextHolder.getBean(JobManager.class).setJobSendResult(sendJobResultR);
        return R.ok(jobInnerCmd.getJobId());
    }
}
