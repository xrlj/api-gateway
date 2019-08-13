package com.xrlj.apigateway.feign;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RolePermissionsClientFallbackFactory implements FallbackFactory<RolePermissionsClient> {

    @Override
    public RolePermissionsClient create(Throwable throwable) {
        log.error("请求远程服务{}降级", RolePermissionsClient.class.getSimpleName());
        return null;
    }
}
