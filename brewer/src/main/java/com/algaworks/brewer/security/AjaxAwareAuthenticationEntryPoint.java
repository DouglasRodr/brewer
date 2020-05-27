package com.algaworks.brewer.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

public class AjaxAwareAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
    public AjaxAwareAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String ajaxHeader = ((HttpServletRequest) request).getHeader("X-Requested-With");
        String contentType = ((HttpServletRequest) request).getContentType();

        if ("XMLHttpRequest".equals(ajaxHeader)) {
            response.sendError(901, "Ajax Request Denied (Session Expired)");
        } else if (contentType != null && contentType.contains("multipart/form-data")){
        	response.sendError(901, "Multipart Request Denied (Session Expired)");
        } else {
            super.commence(request, response, authException);
        }
    }
}
