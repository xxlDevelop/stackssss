package org.yx.hoststack.center.jobs.cmd.image;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateImageCmdData {
    private String idc;
    private String imageId;
    private String imageName;
    private String imageVer;
    private String downloadUrl;
    private String md5;
    private String bucket;
}
