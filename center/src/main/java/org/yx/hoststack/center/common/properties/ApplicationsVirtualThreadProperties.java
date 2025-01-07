package org.yx.hoststack.center.common.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @Description : Custom Configuration - Volume related Configuration
 * @Author : Lee666
 * @Date : 2025/1/4
 * @Version : 1.0
 */
@RefreshScope
@AllArgsConstructor
@Data
@Getter
@Setter
@EqualsAndHashCode
@Configuration
@ToString
@ConfigurationProperties(prefix = "applications.virtual-thread")
public class ApplicationsVirtualThreadProperties {

    public ApplicationsVirtualThreadProperties() {
        this.pool = new Pool();
    }

    private Pool pool;

    @Data
    public static class Pool {

        /**
         * Enable status
         */
        private Boolean enable;
        /**
         * Naming prefix
         */
        private String namePrefix;
        /**
         * Core Pool Size
         */
        private Integer coreSize;
        /**
         * Max Pool Size
         */
        private Integer maxSize;
        /**
         * Queue capacity
         */
        private Integer queueCapacity;

        public Pool(@DefaultValue(value = "false") Boolean enable,
                    @DefaultValue(value = "Virtual-") String namePrefix,
                    @DefaultValue(value = "10") Integer coreSize,
                    @DefaultValue(value = "100") Integer maxSize,
                    @DefaultValue(value = "100") Integer queueCapacity) {
            this.enable = enable;
            this.namePrefix = namePrefix;
            this.coreSize = coreSize;
            this.maxSize = maxSize;
            this.queueCapacity = queueCapacity;
        }

        public Pool() {
            this.enable = false;
            this.namePrefix = "Virtual-";
            this.coreSize = 1;
            this.maxSize = 10;
            this.queueCapacity = 100;
        }

    }

}