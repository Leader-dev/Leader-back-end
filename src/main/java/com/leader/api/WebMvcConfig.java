package com.leader.api;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Configuration
@EnableWebMvc
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

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
            .serializerByType(ObjectId.class, new ToStringSerializer());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(builder.build());
        converters.add(converter);
    }
}
