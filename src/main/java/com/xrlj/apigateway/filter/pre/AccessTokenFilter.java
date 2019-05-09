package com.xrlj.servicesysgenid.filter.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 检查请求中是否有accessToken参数，没有不可访问。
 */
@Component
public class AccessTokenFilter extends ZuulFilter {

    private static Logger logger = LoggerFactory.getLogger(AccessTokenFilter.class);

    /**
     * 四种请求类型。
     * <li>pre：可以在请求被路由之前调用</li>
     * <li>routing：在路由请求时候被调用</li>
     * <li>post：在routing和error过滤器之后被调用</li>
     * <li>error：处理请求时发生错误时被调用</li>
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 决定过滤器执行优先级。数值越小优先级越高。
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 是否执行过滤器。
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
//        int a = 9/0;

        logger.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        /*Object accessToken = request.getParameter("token");
        if(accessToken == null) {
            logger.warn("access token is empty");
            ctx.setSendZuulResponse(false); //过滤该请求，不进行路由
            ctx.setResponseStatusCode(401);//设置了其返回的错误码
            ctx.getResponse().setContentType("application/json;charset=UTF-8");

            forward(request,ctx.getResponse());
        }
        logger.info("access token ok");*/

        return null;
    }

    private void forward(HttpServletRequest request,HttpServletResponse response){
        RequestDispatcher dispatcher = request.getRequestDispatcher("/api/errorToken");
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
