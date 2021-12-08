package com.parrer.deskspaceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.parrer")
public class DeskspaceserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeskspaceserverApplication.class, args);
    }

}
