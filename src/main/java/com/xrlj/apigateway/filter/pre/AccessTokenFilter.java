package com.xrlj.apigateway.filter.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.xrlj.utils.authenticate.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;

/**
 * 检查请求中是否有accessToken参数，没有不可访问。
 */
@Component
@RefreshScope
public class AccessTokenFilter extends ZuulFilter {

    private static Logger logger = LoggerFactory.getLogger(AccessTokenFilter.class);

    @Value("${jwt.sign.secret}")
    private String jwtSecret;

    private static final String AUTHORIZATION_HEADER = "Authorization";

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
        return true;
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();

            String urlStr = request.getRequestURL().toString();
            logger.info("{} request to {}", request.getMethod(), urlStr);

            URL url = new URL(urlStr);
            String requestPath = url.getPath();
            String authorization = request.getHeader(AUTHORIZATION_HEADER);
            if (authorization == null && "/usercentral/user/login".equals(requestPath)) { //请求的是登录接口，直接放行
                return null;
            }

            if (authorization == null) {
                logger.warn("access token is empty");
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                ctx.setResponseStatusCode(401);//设置了其返回的错误码
                ctx.getResponse().setContentType("application/json;charset=UTF-8");
                forward(request, ctx.getResponse(), "/api/nonToken");
            }

            String token = StringUtils.removeStart(authorization, "Bearer ");
            //校验token
            JwtUtils.VerifyTokenResult verifyTokenResult = JwtUtils.verifyToken(jwtSecret, token);
            if (verifyTokenResult.equals(JwtUtils.VerifyTokenResult.VERIFY_OK)) { //成功
                logger.info("access token ok");
                return null;
            } else if (verifyTokenResult == JwtUtils.VerifyTokenResult.TOKEN_EXPIRED_ERROR) {
                logger.info("access token expired");
                forward(request, ctx.getResponse(), "/api/expToken");
            } else {
                logger.info("access token invalid error");
                forward(request, ctx.getResponse(), "/api/errorToken");
            }
        } catch (Exception e) {
            logger.error("token处理异常", e);
        }

        return null;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String paht) {
        RequestDispatcher dispatcher = request.getRequestDispatcher(paht);
        if (null != dispatcher) {
            if (!response.isCommitted()) {
                try {
                    dispatcher.forward(request, response);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

}
