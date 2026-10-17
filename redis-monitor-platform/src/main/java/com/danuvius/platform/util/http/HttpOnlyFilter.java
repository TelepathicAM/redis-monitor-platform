package com.danuvius.platform.util.http;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 * 代码层面设置http-only
 */
@WebFilter(filterName = "HttpOnlyFilter", urlPatterns = "/*")
@Component
public class HttpOnlyFilter implements Filter {

    @Value("${enableHttpOnly:false}")
    private boolean isOpen;

    private ArrayList<String> excludes = new ArrayList<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!isOpen) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            Cookie cookie = cookies[0];
            if (cookie != null) {
                String value = cookie.getValue();
                StringBuilder sb = new StringBuilder();
                sb.append("JSESSIONID=" + value + ";");
                sb.append("Secure;");
                sb.append("HttpOnly;");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR, 1);
                Date date = cal.getTime();
                Locale locale = Locale.CHINA;
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", locale);
                sb.append("Expires=" + sdf.format(date));
                res.setHeader("Set-Cookie", sb.toString());
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 这里不应该写死，应该是配置化方式
        excludes.add("/static/**");
    }
}
