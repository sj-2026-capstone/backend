package com.sjcapstone;

import com.sjcapstone.global.config.JpaAuditingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(JpaAuditingConfig.class)
public class CapstoneApplication {

    public static void main(String[] args) {

        SpringApplication.run(CapstoneApplication.class, args);
    }

}
