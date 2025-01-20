package org.yx.hoststack.center.jobs;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import org.springframework.stereotype.Component;
import org.yx.hoststack.common.utils.MachineIdUtils;
import org.yx.lib.utils.util.StringUtil;

@Component
public class JobIdGenerator {

    private final SnowflakeGenerator snowflakeGenerator;

    public JobIdGenerator() {
        String machineId = MachineIdUtils.getUniqueMachineId();
        long id = StringUtil.stringToNumber(machineId, 31);
        snowflakeGenerator = new SnowflakeGenerator(id, id);
    }

    public String generateJobId() {
        return snowflakeGenerator.next().toString();
    }
}
