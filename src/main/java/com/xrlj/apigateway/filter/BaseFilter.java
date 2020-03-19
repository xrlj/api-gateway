package com.xrlj.apigateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.xrlj.utils.security.Base64Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public abstract class BaseFilter extends ZuulFilter {

    protected static final String AUTHORIZATION_HEADER = "Authorization";

    protected void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        if (null != dispatcher) {
            if (!response.isCommitted()) {
                dispatcher.forward(request, response);
            }
        }
    }

    protected String getToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        String token = StringUtils.removeStart(authorization, "Bearer ");
        return token;
    }

    protected String getLoginUsername(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        String loginInfoB4 = StringUtils.removeStart(authorization, "Basic ");
        String loginInfo = Base64Utils.base64Decode(loginInfoB4);
        String username = loginInfo.substring(0, loginInfo.indexOf(":"));
        return username;
    }

    protected String getClientId(HttpServletRequest request) {
        String clientId = request.getHeader("Client-Id");
        return clientId;
    }

    protected String getClientDeviceType(HttpServletRequest request) {
        String clientDeviceType = request.getHeader("Client-Device-Type");
        return clientDeviceType;
    }
}
