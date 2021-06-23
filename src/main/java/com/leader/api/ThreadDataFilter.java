package com.leader.api;

import com.leader.api.util.component.ThreadDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

@Component
public class ThreadDataFilter implements Filter {

    private final ThreadDataUtil threadDataUtil;

    @Autowired
    public ThreadDataFilter(ThreadDataUtil threadDataUtil) {
        this.threadDataUtil = threadDataUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        threadDataUtil.initThreadData();
        filterChain.doFilter(request, response);
        threadDataUtil.removeThreadData();
    }
}
