package com.leader.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String AUTH_ERROR_RESPONSE = "{\"code\":403}";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(new HandlerInterceptor() {
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                        HttpSession session = request.getSession();
                        Object userid = session.getAttribute("user_id");
                        if (userid == null) {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            response.getOutputStream().print(AUTH_ERROR_RESPONSE);
                            return false;
                        }
                        return true;
                    }
                })
                .addPathPatterns("/**")
                .excludePathPatterns("/user/**");
    }
}
