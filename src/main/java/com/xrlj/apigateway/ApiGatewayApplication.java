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
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
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
//@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {FeignConfiguration.class}),basePackages = {"com.xrlj.apigateway", "com.xrlj.framework"})
@ComponentScan(basePackages = {"com.xrlj.apigateway", "com.xrlj.framework"})
@EnableDiscoveryClient
@EnableEurekaClient //可注册到服务中心
@EnableFeignClients
@EnableZuulProxy
@RefreshScope
@EnableConfigurationProperties
@EnableRedisHttpSession
public class ApiGatewayApplication extends BaseSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info(">>>>>服务{}启动成功：{}", ApiGatewayApplication.class.getSimpleName(), args);
    }

    @RestController
    @RequestMapping("/api")
    class ApiController {

        @Autowired
        private MessageSource messageSource;

        @RequestMapping(value = "/error")
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
        @RequestMapping(value = "/nonToken")
        public ApiResult nonToken(HttpServletResponse response) {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(401, "缺少api验证参数token");
            return apiResult;
        }

        @RequestMapping(value = "/expToken")
        public ApiResult expToken(HttpServletResponse response) {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(410, "令牌已过期,请重新获取");
            return apiResult;
        }

        @RequestMapping(value = "/errorToken")
        public ApiResult errorToken(HttpServletResponse response) {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(411, "无效令牌,请重新获取");
            return apiResult;
        }

        @RequestMapping(value = "/tokenMiss")
        public ApiResult tokenMissing(HttpServletResponse response) {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(412, "已退出登录，请重新登录");
            return apiResult;
        }

        @RequestMapping(value = "/permissionMiss")
        public ApiResult permissionMiss() {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(HttpStatus.METHOD_NOT_ALLOWED.value(), "对接口无访问权限");
            return apiResult;
        }

        @RequestMapping(value = "/checkClientId")
        public ApiResult checkClientId() {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(6000, "缺少请求头Client-Id");
            return apiResult;
        }

        @RequestMapping(value = "/checkClientDeviceType")
        public ApiResult checkClientDeviceType() {
            ApiResult apiResult = new ApiResult();
            apiResult.failure(6001, "缺少请求头Client-Device-Type");
            return apiResult;
        }
    }
}
