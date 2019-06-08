package com.xrlj.apigateway.filter.pre;

import com.netflix.zuul.context.RequestContext;
import com.xrlj.apigateway.config.DirectPath;
import com.xrlj.apigateway.filter.BaseFilter;
import com.xrlj.framework.dao.RedisDao;
import com.xrlj.utils.StringUtil;
import com.xrlj.utils.authenticate.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.List;

/**
 * 检查请求中是否有accessToken参数，没有不可访问。
 */
@Component
@RefreshScope
public class AccessTokenFilter extends BaseFilter {

    private static Logger logger = LoggerFactory.getLogger(AccessTokenFilter.class);

    @Value("${jwt.sign.secret}")
    private String jwtSecret;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private DirectPath directPath;

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
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        try {
            String urlStr = request.getRequestURL().toString();
            logger.info("{} request to {}", request.getMethod(), urlStr);

            URL url = new URL(urlStr);
            String requestPath = url.getPath();
            String authorization = request.getHeader(AUTHORIZATION_HEADER);
            List<String> directPaths = directPath.getDirectPath();
            if (authorization == null && directPaths.contains(requestPath)) { //请求的是登录接口，直接放行
                return null;
            }

            if (authorization == null) {
                logger.warn("access token is empty");
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/nonToken");
                return null;
            }

            String token = StringUtils.removeStart(authorization, "Bearer ");
            String username = JwtUtils.getPubClaimValue(token, "username", String.class);

            String redisJwt = (String) redisDao.get("my:jwt:".concat(username));
            if (StringUtil.isEmpty(redisJwt)) {
                logger.warn("access token is empty");
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/tokenMiss");
                return null;
            }

            //校验token
            JwtUtils.VerifyTokenResult verifyTokenResult = JwtUtils.verifyToken(jwtSecret, token);
            if (verifyTokenResult.equals(JwtUtils.VerifyTokenResult.VERIFY_OK)) { //成功
                logger.info("access token ok");
                return null;
            } else if (verifyTokenResult == JwtUtils.VerifyTokenResult.TOKEN_EXPIRED_ERROR) {
                logger.info("access token expired");
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/expToken");
            } else {
                logger.info("access token invalid error");
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/errorToken");
            }
        } catch (Exception e) {
            logger.error("token处理异常", e);
            ReflectionUtils.rethrowRuntimeException(e);
        }

        return null;
    }
}
