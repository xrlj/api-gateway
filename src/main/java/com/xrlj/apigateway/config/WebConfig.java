package com.xrlj.apigateway.config;

import com.xrlj.framework.spring.config.web.AbstractWebConfiguration;
import com.xrlj.framework.spring.config.web.JsonHandlerExceptionResolverOpen;
import com.xrlj.framework.spring.config.web.JsonViewHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 采用继承WebMvcConfigurationSupport方式。
 * 这种方式会屏蔽springboot的@EnableAutoConfiguration中的设置
 */
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Autowired
    @Qualifier("jsonViewHttpMessageConverterOpen")
    private JsonViewHttpMessageConverter jsonViewHttpMessageConverterOpen;

    @Autowired
    private JsonHandlerExceptionResolverOpen jsonHandlerExceptionResolverOpen;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
       converters.add(jsonViewHttpMessageConverterOpen);
    }

    /**
     * 统一异常处理。
     * @param exceptionResolvers
     */
    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(jsonHandlerExceptionResolverOpen);
    }


}
