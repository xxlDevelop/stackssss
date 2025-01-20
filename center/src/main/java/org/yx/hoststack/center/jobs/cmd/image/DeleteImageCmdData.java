package org.yx.hoststack.center.jobs.cmd.image;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DeleteImageCmdData {
    private String idc;
    private String imageId;
    private String bucket;
}
