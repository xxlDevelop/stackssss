package org.yx.hoststack.protocol.ws.agent.jobs.container;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateContainerJob {

    /**
     * vmType : kvm
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
     * profileTemplate :
     * containerList : [{"profile":"","cid":"1234567890"}]
     */

    private String vmType;
    private Image image;
    private String profileTemplate;
    private List<Container> containerList;

    @Data
    @Builder
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

    @Data
    @Builder
    public static class Container {
        /**
         * profile :
         * cid : 1234567890
         */

        private String profile;
        private String cid;
    }
}
