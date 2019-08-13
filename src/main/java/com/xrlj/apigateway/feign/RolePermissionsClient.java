package com.xrlj.apigateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${service-usercentral.name}", fallbackFactory = AuthClientFallbackFactory.class)
public interface RolePermissionsClient {

    @RequestMapping(value = "${service-usercentral.rolePermissions.checkAuthorizeMethod}", method = RequestMethod.GET)
    Boolean checkAuthorizeMethod(@RequestParam String urlPath);
}
