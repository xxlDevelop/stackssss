package org.yx.hoststack.center;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.yx.hoststack.center.ws.CenterServer;

import java.util.Locale;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableAsync
@MapperScan("org.yx.hoststack.center.mapper")
public class CenterApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        SpringApplication springApplication = new SpringApplication(CenterApplication.class);
        ConfigurableApplicationContext context = springApplication.run(args);
        context.getBean(CenterServer.class).start();
    }

}
