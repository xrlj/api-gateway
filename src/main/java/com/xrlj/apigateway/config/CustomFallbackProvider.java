package com.xrlj.apigateway.config;

import com.alibaba.fastjson.JSONObject;
import com.xrlj.apigateway.common.Constants;
import com.xrlj.framework.spring.mvc.api.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * 请求无响应降级返回。请求超时或者服务没启动，将调用，降级返回结果。
 *
 */
@Component
public class CustomFallbackProvider implements FallbackProvider {

    private static Logger logger = LoggerFactory.getLogger(CustomFallbackProvider.class);

    @Autowired
    private MessageSource messageSource;

    @Override
    public ClientHttpResponse fallbackResponse(String route,Throwable cause) {
        String msg = cause.getMessage();
        logger.error(String.format("服务%s不可用，降级：%s",route,msg));
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.OK;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return HttpStatus.OK.value();
            }

            @Override
            public String getStatusText() throws IOException {
                return HttpStatus.OK.getReasonPhrase();
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                ApiResult apiResult = new ApiResult();
                apiResult.failure(500,messageSource.getMessage("error.msg.service.unavailable",null,Locale.getDefault()));
                String str = JSONObject.toJSONString(apiResult);
                return new ByteArrayInputStream(str.getBytes("UTF-8"));
            }


            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                //和body中的内容编码一致，否则容易乱码
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                headers.add(Constants.SERVICE_UNAVAILABLE_KEY,Constants.SERVICE_UNAVAILABLE_V);
                return headers;
            }
        };
    }

    /**
     * 针对此微服务做回退处理。如果所有服务都做回退，返回“*”或者返回null。
     * @return 返回在注册中心注册的服务name或者服务id
     */
    @Override
    public String getRoute() {
        return null;
    }
}
