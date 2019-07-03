package com.xrlj.apigateway.config;

import com.xrlj.framework.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;

import java.util.List;

/**
 * 采用继承WebMvcConfigurationSupport方式。
 * 这种方式会屏蔽springboot的@EnableAutoConfiguration中的设置
 */
@Configuration
public class WebConfig extends AbstractOutWebConfiguration {

    /**
     * 统一异常处理。
     * @param exceptionResolvers
     */
    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new JsonHandlerExceptionResolverOpen());
    }

    /**
     * favorPathExtension表示支持后缀匹配，
     *
     * 属性ignoreAcceptHeader默认为fasle，表示accept-header匹配，defaultContentType开启默认匹配。
     * @param configurer
     */
    @Override
    protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }
}
