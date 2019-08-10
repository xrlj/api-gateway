package com.xrlj.apigateway.filter.pre;

import com.netflix.zuul.context.RequestContext;
import com.xrlj.apigateway.filter.BaseFilter;
import org.jooq.meta.derby.sys.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 检查请求权限。
 */
@Component
@RefreshScope
public class PermissionFilter extends BaseFilter {

    private static Logger logger = LoggerFactory.getLogger(PermissionFilter.class);

    /**
     * 四种请求类型。
     * <li>pre：可以在请求被路由之前调用</li>
     * <li>routing：在路由请求时候被调用</li>
     * <li>post：在routing和error过滤器之后被调用</li>
     * <li>error：处理请求时发生错误时被调用</li>
     *
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 决定过滤器执行优先级。数值越小优先级越高。
     *
     * @return
     */
    @Override
    public int filterOrder() {
        return 1;
    }

    /**
     * 是否执行过滤器。
     *
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        try {
            System.out.println(">>>>>");
        } catch (Exception e) {
            logger.error("token处理异常", e);
            ReflectionUtils.rethrowRuntimeException(e);
        }

        return null;
    }
}
