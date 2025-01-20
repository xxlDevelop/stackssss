package org.yx.hoststack.center.jobs.cmd.container;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UpdateProfileCmdData {
    private String hostId;
    private List<ContainerProfileInfo> profileInfoList;

    @Getter
    @Setter
    @Builder
    public static class ContainerProfileInfo {
        private String cid;
        private String profile;
    }
}
