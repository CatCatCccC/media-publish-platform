package com.media.publish;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.media.publish.**.infrastructure")
public class MediaPublishApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaPublishApplication.class, args);
    }
}
