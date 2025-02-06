package org.yx.hoststack.center;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.JobTypeEnum;
import org.yx.hoststack.center.jobs.AgentServiceGroup;
import org.yx.hoststack.center.jobs.JobManager;
import org.yx.hoststack.center.jobs.SendJobResult;
import org.yx.hoststack.center.jobs.cmd.JobCmd;
import org.yx.hoststack.center.jobs.cmd.JobCmdChain;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.container.CreateContainerCmdData;
import org.yx.hoststack.center.jobs.cmd.container.CtrlContainerCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.CreateVolumeCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.MountVolumeCmdData;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;

import java.util.List;
import java.util.Map;

@SpringBootTest
class CenterApplicationTests {

    @Builder
    @Getter
    public static class TestData {
        private int key;
        List<Integer> values;
    }

    @Test
    void jobTest() {
//        Map<Integer, TestData> map = Maps.newHashMap();
//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 5; j++) {
//                int finalJ = j;
//                int finalI = i;
//                map.compute(i, (k, v) -> {
//                    if (v == null) {
//                        return TestData.builder()
//                                .key(finalI)
//                                .values(Lists.newArrayList(finalJ))
//                                .build();
//                    } else {
//                        v.getValues().add(finalJ);
//                        return v;
//                    }
//                });
//            }
//        }


        long tenantId = 1234;
        String hostId = "hostId1";
        String cid = "hostId1#01";
        String cid2 = "hostId1#02";
        String cid3 = "hostId1#03";
        String userVolumeId = "userVolumeId1";
        String baseVolumeId = "baseVolumeId1";
        String diskType = "local";


        JobCmdChain jobCmdChain = new JobCmdChain();
        // create parent
        jobCmdChain.next(JobCmd.builder()
                        .tenantId(tenantId)
                        .jobType(JobTypeEnum.PARENT)
                        .jobSubType(JobSubTypeEnum.PARENT_FOR_CREATE_CONTAINER)
                        .build())
                // create user volume
                .next(JobCmd.builder()
                        .tenantId(tenantId)
                        .jobType(JobTypeEnum.VOLUME)
                        .jobSubType(JobSubTypeEnum.CREATE)
                        .jobData(CreateVolumeCmdData.builder()
                                .hostId(hostId)
                                .volumeSize(100)
                                .volumeType("user")
                                .diskType(diskType)
                                .volumeId(Lists.newArrayList(userVolumeId))
                                .build())
                        .build())
                // create base volume
                .next(JobCmd.builder()
                        .tenantId(tenantId)
                        .jobType(JobTypeEnum.VOLUME)
                        .jobSubType(JobSubTypeEnum.CREATE)
                        .jobData(CreateVolumeCmdData.builder()
                                .hostId(hostId)
                                .volumeSize(100)
                                .volumeType("base")
                                .diskType(diskType)
                                .volumeId(Lists.newArrayList(baseVolumeId))
                                .build())
                        .build())
                // create container
                .next(JobCmd.builder()
                        .tenantId(tenantId)
                        .jobType(JobTypeEnum.CONTAINER)
                        .jobSubType(JobSubTypeEnum.CREATE)
                        .jobData(CreateContainerCmdData.builder()
                                .hostId(hostId)
                                .vmType("kvm")
                                .profileTemplate("profileTemplate")
                                .image(CreateContainerCmdData.Image.builder()
                                        .id("imageId1")
                                        .url("http://downloadUrl")
                                        .type("x86-standard-render")
                                        .name("imageName")
                                        .ver("1.1")
                                        .md5("md5")
                                        .user("user")
                                        .password("password")
                                        .sourceType("s3")
                                        .build())
                                .profileInfoList(Lists.newArrayList(
                                        CreateContainerCmdData.ContainerProfileInfo.builder()
                                                .cid(cid)
                                                .profile("profile")
                                                .build(),
                                        CreateContainerCmdData.ContainerProfileInfo.builder()
                                                .cid(cid2)
                                                .profile("profile")
                                                .build(),
                                        CreateContainerCmdData.ContainerProfileInfo.builder()
                                                .cid(cid3)
                                                .profile("profile")
                                                .build()
                                ))
                                .build())
                        .build())
                // mount user and base volume
                .next(JobCmd.builder()
                        .tenantId(tenantId)
                        .jobType(JobTypeEnum.VOLUME)
                        .jobSubType(JobSubTypeEnum.MOUNT)
                        .jobData(MountVolumeCmdData.builder()
                                .hostId(hostId)
                                .mountInfoList(Lists.newArrayList(
                                        MountVolumeCmdData.MountVolumeInfo.builder()
                                                .volumeId(userVolumeId)
                                                .baseVolumeId(baseVolumeId)
                                                .cid(cid)
                                                .mountType(diskType)
                                                .build()))
                                .build())
                        .build())
                // start container
                .next(JobCmd.builder()
                        .tenantId(tenantId)
                        .jobType(JobTypeEnum.CONTAINER)
                        .jobSubType(JobSubTypeEnum.START)
                        .jobData(CtrlContainerCmdData.builder()
                                .hostId(hostId)
                                .ctrl(JobSubTypeEnum.START.getName())
                                .cIds(Lists.newArrayList(cid))
                                .build())
                        .build());
        JobInnerCmd<?> jobInnerCmd = SpringContextHolder.getBean(JobManager.class).createJobs(jobCmdChain);
        if (jobInnerCmd != null) {
            R<SendJobResult> sendJobResultR = SpringContextHolder.getBean(JobManager.class).sendJob(jobInnerCmd);
            SpringContextHolder.getBean(JobManager.class).setJobSendResult(sendJobResultR);
            System.out.println(jobInnerCmd.getJobId());
        }
    }

}
