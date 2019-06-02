package com.xrlj.apigateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.xrlj.apigateway.filter.pre.AccessTokenFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(BaseFilter.class);

    protected void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        if (null != dispatcher) {
            if (!response.isCommitted()) {
                dispatcher.forward(request, response);
            }
        }
    }
}
