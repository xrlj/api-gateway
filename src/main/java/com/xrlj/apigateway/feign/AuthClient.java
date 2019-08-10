package com.xrlj.apigateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${service-auth.name}", fallbackFactory = AuthClientFallbackFactory.class)
public interface AuthClient {

    @RequestMapping(value = "${service-auth.auth.checkPermissions}", method = RequestMethod.GET, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    boolean checkPermissions(@RequestParam String[] permissionNames);

    @RequestMapping(value = "${service-auth.auth.checkRoles}", method = RequestMethod.GET)
    boolean checkRoles(@RequestParam String[] roleNames);

    @RequestMapping(value = "/auth/test",  method = RequestMethod.GET)
    String test(@RequestParam String test);
}
