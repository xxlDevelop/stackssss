package org.yx.hoststack.center.jobs.cmd.image;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DeleteImageCmdData {
    private String imageId;
    private List<IdcInfo> idcInfos;

    @Getter
    @Setter
    @Builder
    public static class IdcInfo {
        private String idc;
        private String bucket;
    }
}
