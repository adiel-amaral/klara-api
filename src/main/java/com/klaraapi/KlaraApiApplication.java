package com.klaraapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableRetry
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class KlaraApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KlaraApiApplication.class, args);
    }

}
