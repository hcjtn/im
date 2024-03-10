package edu.nuc.hcj.im.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;


@SpringBootApplication(scanBasePackages = {"edu.nuc.hcj.im.service","edu.nuc.hcj.im.common"})
@MapperScan("edu.nuc.hcj.im.service.*.dao.mapper")

public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

}
