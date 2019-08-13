package com.xrlj.apigateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "${service-usercentral.name}", fallbackFactory = AuthClientFallbackFactory.class)
public interface RolePermissionsClient {

    @RequestMapping(value = "${service-usercentral.rolePermissions.checkAuthorizeMethod}", method = RequestMethod.GET, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    Boolean checkAuthorizeMethod(String urlPath);
}
