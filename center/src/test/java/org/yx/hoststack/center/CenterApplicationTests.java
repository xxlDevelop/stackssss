package org.yx.hoststack.center;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.JobTypeEnum;
import org.yx.hoststack.center.jobs.JobManager;
import org.yx.hoststack.center.jobs.cmd.JobCmd;
import org.yx.hoststack.center.jobs.cmd.JobCmdChain;
import org.yx.hoststack.center.jobs.cmd.container.CreateContainerCmdData;
import org.yx.hoststack.center.jobs.cmd.container.CtrlContainerCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.CreateVolumeCmdData;
import org.yx.hoststack.center.jobs.cmd.volume.MountVolumeCmdData;
import org.yx.lib.utils.util.SpringContextHolder;

@SpringBootTest
class CenterApplicationTests {

    @Test
    void jobTest() {
        long tenantId = 110;
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
        String jobId = SpringContextHolder.getBean(JobManager.class).createJobs(jobCmdChain);
        System.out.println(jobId);
    }

}
