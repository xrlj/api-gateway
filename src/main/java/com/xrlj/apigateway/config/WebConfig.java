package com.xrlj.apigateway.config;

import com.xrlj.framework.spring.config.JsonHandlerExceptionResolverOpen;
import com.xrlj.framework.spring.config.JsonHttpMessageConverter2;
import com.xrlj.framework.spring.config.JsonViewHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 采用继承WebMvcConfigurationSupport方式。
 * 这种方式会屏蔽springboot的@EnableAutoConfiguration中的设置
 */
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Autowired
    private JsonViewHttpMessageConverter jsonViewHttpMessageConverter;

    /**
     * 配置消息转换规则。
     *
     * @param converters
     */
    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //------json与对象转换器
        converters.add(jsonViewHttpMessageConverter);
        converters.add(new JsonHttpMessageConverter2());
    }

    /**
     * 全局验证器
     *
     * @return
     */
    @Override
    protected Validator getValidator() {
        return super.getValidator();
    }

    /*@Override
    protected void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldAnnotation(new SensitiveFormatAnnotationFormatterFactory(s -> {
            if ("色情".equals(s)) {
                return "参数中包含敏感词";
            }
            return s;
        }));
        super.addFormatters(registry);
    }*/



    /**
     * 统一异常处理。
     * @param exceptionResolvers
     */
    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new JsonHandlerExceptionResolverOpen());
    }


}
