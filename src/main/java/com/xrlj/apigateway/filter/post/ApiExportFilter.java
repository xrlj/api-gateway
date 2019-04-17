package com.xrlj.apigateway.filter.post;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.xrlj.apigateway.common.Constants;
import com.xrlj.framework.spring.mvc.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import com.netflix.util.Pair;

/**
 * 没有异常。处理服务返回的结果，以更加友好格式返回客户端。
 */
@Slf4j
@Component
public class ApiExportFilter extends ZuulFilter {

    @Autowired
    private MessageSource messageSource;


    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        boolean e_b = !ctx.containsKey("error.status_code"); //api-gateway服务没有发生内部代码异常执行
        int status = ctx.getResponse().getStatus();
        return e_b && status != 401;
    }

    @Override
    public Object run() {
        try {
            RequestContext context = RequestContext.getCurrentContext();
            HttpServletResponse servletResponse = context.getResponse();
            //设置响应内容类型
            servletResponse.setHeader("Content-Type","application/json; charset=utf-8");
            int serviceStatus = servletResponse.getStatus(); //实际服务请求状态
            String serviceName = ""; //服务名称

            //检查本服务和实际请求服务响应头信息。
            List<Pair<String, String>> zuulRespHeaders = context.getZuulResponseHeaders();//请求头文件信息
            boolean innerServiceError = false;//true=实际调用服务内部异常
            for (Pair<String,String> p : zuulRespHeaders) {
                String key = p.first();
                //服务是否可用，服务不可用，降级了，直接返回降级处理内容
                if (Constants.SERVICE_UNAVAILABLE_KEY.equals(key)) {
                    return null;
                }
                //获取实际请求服务名称
                if ("m-service-name".equals(key)) {
                    serviceName = p.second();
                }
                //服务是否抛出异常
                if ("m-error-type".equals(key)) {
                    String value = p.second();
                    if ("service-inner".equals(value)) {
                        innerServiceError = true;
                    }
                }
            }

            //处理服务返回
            InputStream respData = context.getResponseDataStream();
            String respDataStr = (respData == null) ? "{}" : IOUtils.toString(respData,"utf-8");
            log.debug(String.format(">>>>请求服务%s返回：%s",serviceName,respDataStr));
            ApiResult<Object> apiResult = new ApiResult<>();
            if (innerServiceError || serviceStatus != HttpStatus.OK.value()) { //服务异常返回
                JSONObject jsonObject = JSONObject.parseObject(respDataStr);
                String message = jsonObject.getString("message");
                Integer statusCode = jsonObject.getInteger("status");
                apiResult.setCode(statusCode);
                apiResult.setSuccess(false);
                apiResult.setDescription(message);
                apiResult.setData("");
            } else { //正常返回
                servletResponse.addHeader("m-error-type","no");
                apiResult.setSuccess(true);
                apiResult.setCode(HttpStatus.OK.value());
                apiResult.setDescription(messageSource.getMessage("success.msg.description",null,Locale.getDefault()));
                if (respDataStr == null || "".equals(respDataStr)) {
                    apiResult.setData("");
                } else if (respDataStr.startsWith("{") || respDataStr.startsWith("[")) {
                    apiResult.setData(JSON.parse(respDataStr));
                } else {
                    apiResult.setData(respDataStr);
                }

            }

            context.setResponseBody(apiResult.toString());

        } catch (Exception e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }
        return null;
    }


}
