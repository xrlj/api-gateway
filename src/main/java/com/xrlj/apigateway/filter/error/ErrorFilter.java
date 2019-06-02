package com.xrlj.apigateway.filter.error;

import com.netflix.zuul.context.RequestContext;
import com.xrlj.apigateway.filter.BaseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * api网关本身系统异常都会进入run方法。
 */
@Component
public class ErrorFilter extends BaseFilter {

    private static Logger logger = LoggerFactory.getLogger(ErrorFilter.class);

    @Override
    public String filterType() {
        return "error";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        Throwable throwable = context.getThrowable();
        logger.error("this is a ErrorFilter：{}",throwable.getCause().getMessage());
        context.set("error.status_code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        context.set("error.message",throwable.getCause().getMessage());
        return null;
    }
}
