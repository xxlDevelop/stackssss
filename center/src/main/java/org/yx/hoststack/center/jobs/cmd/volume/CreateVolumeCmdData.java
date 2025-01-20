package org.yx.hoststack.center.jobs.cmd.volume;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreateVolumeCmdData {

    /**
     * volumeSize : 100
     * volumeType : base/user
     * diskType : local/net
     * sourceUrl : 创建非空存储卷时需要指定数据下载地址
     * md5: md5
     * hostId : hostId
     * snapshotName : snapshotName
     * hostIds: hostIds
     */

    private int volumeSize;
    private String volumeType;
    private String diskType;
    private String sourceUrl;
    private String md5;
    private String hostId;
    private String snapshotName;

    private List<String> volumeId;
}
