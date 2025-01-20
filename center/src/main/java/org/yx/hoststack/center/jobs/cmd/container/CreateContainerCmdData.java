package org.yx.hoststack.center.jobs.cmd.container;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreateContainerCmdData {
    private String hostId;
    private String vmType;
    private Image image;
    private String profileTemplate;
    private List<ContainerProfileInfo> profileInfoList;

    @Getter
    @Setter
    @Builder
    public static class Image {
        private String id;
        private String url;
        private String name;
        private String type;
        private String ver;
        private String md5;
        private String user;
        private String password;
        private String sourceType;
    }

    @Getter
    @Setter
    @Builder
    public static class ContainerProfileInfo {
        private String cid;
        private String profile;
    }
}
