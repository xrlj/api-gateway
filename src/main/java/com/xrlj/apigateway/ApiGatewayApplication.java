package com.xrlj.apigateway;

import com.netflix.zuul.FilterProcessor;
import com.xrlj.apigateway.config.CustomFilterProcessor;
import com.xrlj.framework.spring.mvc.api.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * https://www.jianshu.com/p/ff863d532767
 *
 * 待解决问题：加了安全验证，Authorization密码错误，返回空白。不添加认证，没按设定返回。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.xrlj.apigateway", "com.xrlj.framework"})
@EnableDiscoveryClient
@EnableEurekaClient //可注册到服务中心
@EnableZuulProxy
public class ApiGatewayApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        FilterProcessor.setProcessor(new CustomFilterProcessor());
    }


    @RestController
    @RequestMapping("/api")
    class ApiController {

        @Autowired
        private MessageSource messageSource;

        @GetMapping("/error")
        public ApiResult error(HttpServletResponse response) {
            ApiResult<String> apiResult = new ApiResult<>();
            apiResult.failure(500,messageSource.getMessage("error.msg.system",null,Locale.getDefault()));
            return apiResult;
        }

        @GetMapping("/errorToken")
        public ApiResult errorToken(HttpServletResponse response) {
            ApiResult<String> apiResult = new ApiResult<>();
            apiResult.failure(401,"缺少api验证参数token");
            return apiResult;
        }
    }
}
