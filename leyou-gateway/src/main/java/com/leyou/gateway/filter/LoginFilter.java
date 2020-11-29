package com.leyou.gateway.filter;

import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        // 获取白名单路径
        List<String> allowPaths = this.filterProperties.getAllowPaths();
        // 初始化zuul网关运行上下文
        RequestContext context = RequestContext.getCurrentContext();
        // 获取request对象
        HttpServletRequest request = context.getRequest();
        String requestURL = request.getRequestURL().toString();
        for (String allowPath : allowPaths) {
            if (StringUtils.contains(requestURL,allowPath)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        // 初始化zuul网关运行上下文
        RequestContext context = RequestContext.getCurrentContext();
        // 获取request对象
        HttpServletRequest request = context.getRequest();
        String cookieValue = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        if (StringUtils.isBlank(cookieValue)) {
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        try {
            JwtUtils.getInfoFromToken(cookieValue, this.jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}
