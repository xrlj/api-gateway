package com.xrlj.apigateway.feign;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthClientFallbackFactory implements FallbackFactory<AuthClient> {

    @Override
    public AuthClient create(Throwable throwable) {
        log.error("请求远程服务{}降级", AuthClient.class.getSimpleName(), throwable);
        return new AuthClient() {
            @Override
            public boolean checkPermissions(String[] permissionNames) {
                return false;
            }

            @Override
            public boolean checkRoles(String[] roleNames) {
                return false;
            }

            @Override
            public String test(String test) {
                return null;
            }
        };
    }
}
