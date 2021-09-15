package com.xrlj.apigateway.filter.pre;

import com.netflix.zuul.context.RequestContext;
import com.xrlj.apigateway.config.DirectPath;
import com.xrlj.apigateway.filter.BaseFilter;
import com.xrlj.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.List;

/**
 * 检查请求头中是否带有Client-Id、Client-Device-Type
 */
@Component
@RefreshScope
public class RequestHeadersCheckFilter extends BaseFilter {

    private static Logger logger = LoggerFactory.getLogger(RequestHeadersCheckFilter.class);

    private final DirectPath directPath;

    public RequestHeadersCheckFilter(DirectPath directPath) {
        this.directPath = directPath;
    }

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
        return 0;
    }

    /**
     * 是否执行过滤器。
     *
     * @return
     */
    @Override
    public boolean shouldFilter() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();

            String urlStr = request.getRequestURL().toString();
            logger.info("{} request to {}", request.getMethod(), urlStr);

            URL url = new URL(urlStr);
            String requestPath = url.getPath();
            List<String> directPaths = directPath.getDirectPath();
            if (directPaths.contains(requestPath)) { //直接放行
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("过滤请求头信息处理错误", e);
            return false;
        }
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        try {
            String clientId = request.getHeader("Client-Id");
            String clientDeviceType = request.getHeader("Client-Device-Type");
            if (StringUtil.isEmpty(clientId)) {
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/checkClientId");
            }
            if (StringUtil.isEmpty(clientDeviceType)) {
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/checkClientDeviceType");
            }
        } catch (Exception e) {
            logger.error("检查请求头异常", e);
            ReflectionUtils.rethrowRuntimeException(e);
        }

        return null;
    }
}
