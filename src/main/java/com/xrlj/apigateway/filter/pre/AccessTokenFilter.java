package com.xrlj.apigateway.filter.pre;

import com.netflix.zuul.context.RequestContext;
import com.xrlj.apigateway.common.Constants;
import com.xrlj.apigateway.config.DirectPath;
import com.xrlj.apigateway.filter.BaseFilter;
import com.xrlj.framework.dao.RedisDao;
import com.xrlj.infrastructure.IConstants;
import com.xrlj.utils.StringUtil;
import com.xrlj.utils.authenticate.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.List;

/**
 * 检查认证token。
 */
@Component
@RefreshScope
public class AccessTokenFilter extends BaseFilter {

    private static Logger logger = LoggerFactory.getLogger(AccessTokenFilter.class);

    private final RedisDao redisDao;

    private final DirectPath directPath;

    @Autowired
    public AccessTokenFilter(RedisDao redisDao, DirectPath directPath) {
        this.redisDao = redisDao;
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
            String authorization = request.getHeader(AUTHORIZATION_HEADER);
            List<String> directPaths = directPath.getDirectPath();
            if (directPaths.contains(requestPath)) { //直接放行
                return false;
            }

            if (authorization == null) {
                logger.info("access token is empty");
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/nonToken");
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("token过滤处理异常", e);
            return false;
        }

    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        try {
            String token = getToken(request);

            /*String jwtKey = JwtUtils.getPubClaimValue(token, Constants.JWT.JWT_KEY, String.class);
            //判断token是否已经过期,不能这么判断.这样判断后，只能在一个浏览器登录，一个终端退出，则全退出。
            String redisJwt = (String) redisDao.get(jwtKey);
            if (StringUtil.isEmpty(redisJwt)) {
                logger.warn("access token is empty");
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/tokenMiss");
                return null;
            }
            if (!token.equals(redisJwt)) {
                logger.info("access token invalid error");
                ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
                forward(request, ctx.getResponse(), "/api/errorToken");
                return null;
            }*/

            //====校验token jwtSecret通过请求数据库获取
            //redis透传，不通过网络，加快速度。
            String appSecret = (String) redisDao.get(Constants.JWT.appSecretKey(getClientId(request)));//登录成功后保存
            JwtUtils.VerifyTokenResult verifyTokenResult = JwtUtils.verifyToken(Constants.JWT.JWT_ISSUER, appSecret == null ? "" : appSecret, token);
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
