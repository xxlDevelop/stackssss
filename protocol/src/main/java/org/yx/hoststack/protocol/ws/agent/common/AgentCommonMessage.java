package org.yx.hoststack.protocol.ws.agent.common;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentCommonMessage<T> {
    /*
    {
        "type": "request",
        "method": "CreateVM",
        "hostId": "123456789",
        "traceId": "7E6D2238-57D1-7CDA-36DE-D2B427D7323A",
        "jobId": "1866687880844279808",
        "data": {
            "vmType": "kvm",
            "image": {
                "url": "http://example.com/image.iso",
                "name": "ubuntu-20.04",
                "id": "",
                "ver": "1.1",
                "md5": "d41d8cd98f00b204e9800998ecf8427e",
                "user": "user01",
                "password": "",
                "sourceType": "S3"
            },
            "batch": [
                {
                    "profile": "",
                    "cid": "1234567890"
                }
            ]
        }
    }
     */
    private String type;
    private String method;
    private String hostId;
    private String traceId;
    private String jobId;
    private String status;
    private int progress;
    private Integer code;
    private String msg;
    private T data;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
