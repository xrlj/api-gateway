package com.xrlj.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "need-authorize-interface")
public class AuthorizeIFace {

    private List<String> authorizeIFace = new ArrayList<>();

    public List<String> getAuthorizeIFace() {
        return authorizeIFace;
    }

    public void setAuthorizeIFace(List<String> authorizeIFace) {
        this.authorizeIFace = authorizeIFace;
    }
}
