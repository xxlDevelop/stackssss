package org.yx.hoststack.edge.common;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;
import org.yx.lib.utils.util.StringPool;

import java.util.Map;

@Component
public class CenterJobMappingAgentJob {
    private final Map<String, String> MAPPING = Maps.newHashMap();

    public CenterJobMappingAgentJob() {
        MAPPING.put("host-ResetHost", "ResetHost");
        MAPPING.put("host-UpdateConfig", "UpdateConfig");
        MAPPING.put("host-ExecuteCmd", "ExecuteCmd");

        MAPPING.put("container-Create", "CreateVM");
        MAPPING.put("container-Upgrade", "UpgradeImage");
        MAPPING.put("container-UpdateProfile", "ModifyVM");
        MAPPING.put("container-Ctrl", "ControlVM");
        MAPPING.put("container-ExecCmd", "ExecuteCmd");

        MAPPING.put("volume-Create", "CreateVolume");
        MAPPING.put("volume-Delete", "DeleteVolume");
        MAPPING.put("volume-Mount", "MountVolume");
        MAPPING.put("volume-UnMount", "UnmountVolume");
        MAPPING.put("volume-Upgrade", "UpgradeVolume");
    }

    public String getAgentJobName(String centerJobType, String centerSubJobType) {
        return MAPPING.get(String.format("%s-%s", centerJobType.toLowerCase(), centerSubJobType.toLowerCase()));
    }

    public Map<String, String> getCenterJobName(String agentJobName) {
        for (Map.Entry<String, String> entry : MAPPING.entrySet()) {
            if (entry.getValue().equals(agentJobName)) {
                String jobType = entry.getKey().split(StringPool.DASH)[0];
                String jobSubType = entry.getKey().split(StringPool.DASH)[1];
                Map<String, String> centerJobName = Maps.newHashMap();
                centerJobName.put("jobType", jobType);
                centerJobName.put("jobSubType", jobSubType);
                return centerJobName;
            }
        }
        return null;
    }
}
