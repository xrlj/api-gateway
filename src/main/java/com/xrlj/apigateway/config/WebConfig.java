package com.xrlj.apigateway.config;

import com.xrlj.framework.config.AbstractWebConfiguration;
import com.xrlj.framework.config.JsonHandlerExceptionResolverOpen;
import com.xrlj.framework.config.JsonHttpMessageConverter2;
import com.xrlj.framework.config.JsonViewHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

/**
 * 采用继承WebMvcConfigurationSupport方式。
 * 这种方式会屏蔽springboot的@EnableAutoConfiguration中的设置
 */
@Configuration
public class WebConfig extends AbstractWebConfiguration {

    @Autowired
    @Qualifier("jsonViewHttpMessageConverterOpen")
    private JsonViewHttpMessageConverter jsonViewHttpMessageConverterOpen;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
       converters.add(jsonViewHttpMessageConverterOpen);
        converters.add(new JsonHttpMessageConverter2());
    }

    /**
     * 统一异常处理。
     * @param exceptionResolvers
     */
    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new JsonHandlerExceptionResolverOpen());
    }
}
