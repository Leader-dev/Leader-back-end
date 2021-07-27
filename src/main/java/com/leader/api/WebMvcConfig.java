package com.leader.api;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.leader.api.service.admin.AdminIdService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.member.OrgMemberService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.UserAuthException;
import com.leader.api.util.response.AuthErrorResponse;
import com.leader.api.util.response.InternalErrorResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebMvc
@ControllerAdvice
public class WebMvcConfig implements WebMvcConfigurer {

    public static String ORG_ID_PARAMETER_NAME = "orgId";

    private final UserIdService userIdService;
    private final OrgMemberIdService memberIdService;
    private final OrgMemberService memberService;
    private final AdminIdService adminIdService;

    @Autowired
    public WebMvcConfig(UserIdService userIdService, OrgMemberIdService memberIdService,
                        OrgMemberService memberService, AdminIdService adminIdService) {
        this.userIdService = userIdService;
        this.memberIdService = memberIdService;
        this.memberService = memberService;
        this.adminIdService = adminIdService;
    }

    private static boolean isPostRequest(HttpServletRequest request) {
        return "POST".equals(request.getMethod());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // allow CORS for all paths
        registry
                .addMapping("/**")
                .allowedMethods("GET", "POST", "OPTIONS")
                .exposedHeaders(APITokenFilter.SET_TOKEN_HEADER_KEY);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // add base authentication checker for all routes other than /user/** and /admin/**
        registry
                .addInterceptor(new HandlerInterceptor() {
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        // check if userId exists
                        if (isPostRequest(request) && !userIdService.currentUserExists()) {
                            throw new UserAuthException();
                        }
                        return true;
                    }
                })
                .addPathPatterns("/**")
                .excludePathPatterns("/user/**", "/admin/**", "/api/info", "/service/image/access-start-url")
                .addPathPatterns("/user/info/**");
        // add base orgId parameter handler for all routes in /org/manage/**
        registry
                .addInterceptor(new HandlerInterceptor() {
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        if (isPostRequest(request)) {
                            String orgIdString = request.getParameter(ORG_ID_PARAMETER_NAME);
                            if (orgIdString == null) {
                                throw new InternalErrorException("Missing required parameter " + ORG_ID_PARAMETER_NAME + ".");
                            }
                            ObjectId orgId = new ObjectId(orgIdString);
                            ObjectId userId = userIdService.getCurrentUserId();
                            memberService.assertIsMember(orgId, userId);
                            memberIdService.setOrgId(orgId);
                        }
                        return true;
                    }
                })
                .addPathPatterns("/org/manage/**");
        // add base admin authentication checker
        registry
                .addInterceptor(new HandlerInterceptor() {
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        // check if userId exists
                        if (isPostRequest(request) && !adminIdService.currentAdminExists()) {
                            throw new UserAuthException();
                        }
                        return true;
                    }
                })
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/key", "/admin/login");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Document> handle(Exception ex) {
        // Special handling for user auth failed, return auth error (403)
        if (ex instanceof UserAuthException) {
            return new ResponseEntity<>(new AuthErrorResponse(), HttpStatus.OK);
        }
        // if an exception other than defined types occur, print trace in console for debug
        if (!(ex instanceof InternalErrorException) || ex.getCause() != null) {
            ex.printStackTrace();
        }
        // whenever an exception occur, return internal error (500) along with the original message of the exception
        return new ResponseEntity<>(new InternalErrorResponse(ex.getMessage()), HttpStatus.OK);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // whenever a ObjectId object is encountered in response, convert it to hex string (toString() function does this)
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
            .serializerByType(ObjectId.class, new ToStringSerializer());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(builder.build());
        converters.add(converter);
    }
}
