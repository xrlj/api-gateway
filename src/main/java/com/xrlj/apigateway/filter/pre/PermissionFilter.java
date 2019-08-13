package com.xrlj.apigateway.filter.pre;

import com.netflix.zuul.context.RequestContext;
import com.sun.org.apache.regexp.internal.RE;
import com.xrlj.apigateway.config.DirectPath;
import com.xrlj.apigateway.feign.AuthClient;
import com.xrlj.apigateway.feign.RolePermissionsClient;
import com.xrlj.apigateway.filter.BaseFilter;
import com.xrlj.infrastructure.TokenUtils;
import com.xrlj.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查请求接口授权。
 */
@Component
@RefreshScope
public class  PermissionFilter extends BaseFilter {

    private static Logger logger = LoggerFactory.getLogger(PermissionFilter.class);

    @Autowired
    private AuthClient authClient;

    @Autowired
    private RolePermissionsClient rolePermissionsClient;

    @Autowired
    private DirectPath directPath;

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

            boolean sendZuulResponse = ctx.getBoolean("sendZuulResponse", true);  //有在AccessTokenFilter设置
            return sendZuulResponse;
        } catch (Exception e) {
            logger.error("授权过滤处理异常", e);
            return false;
        }
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();

            String token = getToken(request);

            String urlStr = request.getRequestURL().toString();
            logger.info("{} request to {}", request.getMethod(), urlStr);
            URL url = new URL(urlStr);
            String requestPath = url.getPath();
            String permissionPath = requestPath.replace("/", "-");
            List<String> permissions = new ArrayList<>();
            permissions.add(StringUtil.removeStart(permissionPath, "-"));
            String[] s = new String[permissions.size()];
            permissions.toArray(s);
            boolean b = true; //默认无需授权
            //对外开放系统，所有api都需要授权才能访问
            if (TokenUtils.getAppType(token) == 1) {
                b = authClient.checkPermissions(s);
            } else { //内部系统
                boolean b1 = rolePermissionsClient.checkAuthorizeMethod(requestPath);
                if (b1) { //该接口需要授权
                    b = authClient.checkPermissions(s);
                }
            }
            if (!b) { //没拥有该接口权限
                logger.error("用户{}对接口{}没有访问权限", TokenUtils.getUsername(token), requestPath);
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/permissionMiss");
                return null;
            }
        } catch (Exception e) {
            logger.error("接口授权处理异常", e);
            ReflectionUtils.rethrowRuntimeException(e);
        }

        return null;
    }
}
