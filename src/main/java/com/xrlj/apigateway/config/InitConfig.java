package com.xrlj.apigateway.config;

import com.netflix.zuul.FilterProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InitConfig implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        FilterProcessor.setProcessor(new CustomFilterProcessor());
    }
}
