package com.xrlj.apigateway.feign;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthClientFallbackFactory implements FallbackFactory<AuthClient> {

    @Override
    public AuthClient create(Throwable throwable) {
        return new AuthClient() {
            @Override
            public boolean checkPermissions(String[] permissionNames) {
                return false;
            }

            @Override
            public boolean checkRoles(String[] roleNames) {
                return false;
            }
        };
    }
}
