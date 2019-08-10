package com.xrlj.apigateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${service-auth.name}", fallbackFactory = AuthClientFallbackFactory.class)
public interface AuthClient {

    @GetMapping("${service-auth.auth.checkPermissions}")
    boolean checkPermissions(@RequestParam String[] permissionNames);

    @GetMapping("${service-auth.auth.checkRoles}")
    boolean checkRoles(@RequestParam String[] roleNames);
}
