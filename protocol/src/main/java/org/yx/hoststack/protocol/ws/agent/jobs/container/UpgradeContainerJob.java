package org.yx.hoststack.protocol.ws.agent.jobs.container;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class UpgradeContainerJob {

    /**
     * image : {
     *              "url":"http://example.com/image.iso",
     *              "name":"ubuntu-20.04",
     *              "id":"",
     *              "ver":"1.1",
     *              "md5":"d41d8cd98f00b204e9800998ecf8427e",
     *              "user":"user01",
     *              "password":"asdfghjkl",
     *              "sourceType":"S3"
     *         }
     * cids : ["123456789","456789123"]
     */

    private Image image;
    private List<String> cids;

    @Builder
    @Data
    public static class Image {
        /**
         * url : http://example.com/image.iso
         * name : ubuntu-20.04
         * id :
         * ver : 1.1
         * md5 : d41d8cd98f00b204e9800998ecf8427e
         * user : user01
         * password : asdfghjkl
         * sourceType : S3
         */

        private String url;
        private String name;
        private String id;
        private String ver;
        private String md5;
        private String user;
        private String password;
        private String sourceType;
    }
}
