package com.xrlj.apigateway;

import com.xrlj.framework.base.BaseSpringbootApplication;
import com.xrlj.framework.spring.mvc.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * https://www.jianshu.com/p/ff863d532767
 * <p>
 * 待解决问题：加了安全验证，Authorization密码错误，返回空白。不添加认证，没按设定返回。
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.xrlj.apigateway", "com.xrlj.framework"})
@EnableDiscoveryClient
@EnableEurekaClient //可注册到服务中心
@EnableZuulProxy
@RefreshScope
@EnableConfigurationProperties
@EnableRedisHttpSession
public class ApiGatewayApplication extends BaseSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info(">>>>>服务启动成功：{}", args);
    }

    @RestController
    @RequestMapping("/api")
    class ApiController {

        @Autowired
        private MessageSource messageSource;

        @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST})
        public ApiResult error(HttpServletResponse response) {
            int status = response.getStatus();
            ApiResult apiResult;
            if (status == 404) {
                apiResult = ApiResult.error(404, messageSource.getMessage("error.msg.no.path", null, Locale.getDefault()));
            } else {
                apiResult = ApiResult.error(500, messageSource.getMessage("error.msg.system", null, Locale.getDefault()));
            }
            return apiResult;
        }

        /**
         * 还没登录认证，缺少token。
         *
         * @param response
         * @return
         */
        @RequestMapping(value = "/nonToken", method = {RequestMethod.GET, RequestMethod.POST})
        public ApiResult nonToken(HttpServletResponse response) {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(401, "缺少api验证参数token");
            return apiResult;
        }

        @RequestMapping(value = "/expToken", method = {RequestMethod.GET, RequestMethod.POST})
        public ApiResult expToken(HttpServletResponse response) {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(410, "token已过期,请重新获取token");
            return apiResult;
        }

        @RequestMapping(value = "/errorToken", method = {RequestMethod.GET, RequestMethod.POST})
        public ApiResult errorToken(HttpServletResponse response) {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(411, "无效token,请重新获取token");
            return apiResult;
        }

        @RequestMapping(value = "/tokenMiss", method = {RequestMethod.GET, RequestMethod.POST})
        public ApiResult tokenMissing(HttpServletResponse response) {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(412, "已退出登录，请重新获取token");
            return apiResult;
        }
    }
}
