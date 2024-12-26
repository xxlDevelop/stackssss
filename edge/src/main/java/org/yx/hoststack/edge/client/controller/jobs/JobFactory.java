package org.yx.hoststack.edge.client.controller.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobFactory {
    private final Map<String, HostStackJob> jobMap;

    public HostStackJob get(String jobType) {
        return jobMap.get(jobType);
    }
}
