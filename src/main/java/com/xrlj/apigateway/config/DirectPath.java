package com.xrlj.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关不拦截的url列表。
 */
@Configuration
@ConfigurationProperties(prefix = "access-token-filter")
public class DirectPath {

    private List<String> directPath = new ArrayList<>();

    public List<String> getDirectPath() {
        return directPath;
    }

    public void setDirectPath(List<String> directPath) {
        this.directPath = directPath;
    }
}
