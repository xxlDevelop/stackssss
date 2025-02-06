package org.yx.hoststack.edge.client.controller.jobs;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobCacheData {

    private String jobId;
    private String jobDetailId;
    private String jobType;
    private String jobSubType;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
