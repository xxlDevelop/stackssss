package org.yx.hoststack.center.jobs.cmd.module;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpgradeModuleCmdData {
    private String moduleName;
    private String version;
    private String downloadUrl;
    private String md5;
    private List<String> hostIds;
}
