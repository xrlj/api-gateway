package com.xrlj.apigateway.filter.error;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 仅处理来自post阶段抛出的异常。
 * <P></P>
 * 由于pre阶段，route阶段等的异常最终到会经过post过滤器处理，因此在post处理即可。但是post过滤器
 * 抛出的异常被error过滤器处理后，将不再经过post过滤器处理，因此定义该error过滤器，
 * 但是该过滤器只处理post抛出的异常，其它类型的过滤器异常不处理。
 *
 */
@Component
public class ErrorFromPostFilter extends SendErrorFilter {

    private static Logger logger = LoggerFactory.getLogger(ErrorFromPostFilter.class);

    @Override
    public String filterType() {
        return "error";
    }

    /**
     * 已经禁用{@link SendErrorFilter}过滤器，该过滤的顺序为0，不禁用，会先执行它，它会直接重定向到”/error“直接返回。
     * 返回值要比{@link ErrorFilter}中的大。
     * @return
     */
    @Override
    public int filterOrder() {
        return 30;
    }

    @Override
    public boolean shouldFilter() {
        // 判断：仅处理来自post过滤器引起的异常
        RequestContext ctx = RequestContext.getCurrentContext();
        ZuulFilter failedFilter = (ZuulFilter) ctx.get("failed.filter");
        if(failedFilter != null && failedFilter.filterType().equals("post")) {
            return true;
        }
        return false;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ExceptionHolder exceptions = this.findZuulException(ctx.getThrowable());
        logger.info(String.format("api服务错误：%s",exceptions.getThrowable().getMessage()));
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();
        //----要保证上面代码不会抛出异常，否则返回空白

        if (exceptions != null) {
            try {
                // Remove error code to prevent further error handling in follow up filters
                // 删除该异常信息,不然在下一个过滤器中还会被执行处理
                ctx.remove("throwable");

                //重定向
                forward(request,response);
            } catch (Exception e) {
                logger.error("api内部异常：",e.getMessage());
                forward(request,response);

                ReflectionUtils.rethrowRuntimeException(e);
            }
        }

        return null;
    }

    /**
     * 重定向。该方法内要是出现代码异常的话api接口将会返回空白。还没找到办法处理……
     * @param request
     * @param response
     */
    private void forward(HttpServletRequest request,HttpServletResponse response){
        response.setContentType("application/json;charset=UTF-8");
        response.addHeader("m-error-type", "zuul-filter-post");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/api/error");
        if (dispatcher != null) {
            if (!response.isCommitted()) {
                try {
                    dispatcher.forward(request, response);
                } catch (Exception e) {
                    logger.error("重定向异常，api返回空白：",e.getMessage());
                }
            }
        }
    }

}
