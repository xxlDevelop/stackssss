package org.yx.hoststack.center.jobs;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendJobResult {
    private String jobId;
    private int totalJobCount;
    private List<String> success;
    private List<String> fail;
}
