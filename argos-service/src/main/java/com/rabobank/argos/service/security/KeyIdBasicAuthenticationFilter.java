package com.rabobank.argos.service.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.rabobank.argos.service.security.NonPersonalAccountAuthenticationToken.Credentials;

public class KeyIdBasicAuthenticationFilter extends BasicAuthenticationFilter {
    public KeyIdBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        super.doFilterInternal(request, response, chain);
        /*  if super call is successfull we get a UsernamePasswordAuthenticationToken in
            security context with username and password from basic auth header
        */
        if (SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken parsedTokenFromBasicHeader = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                    .getContext()
                    .getAuthentication();
            NonPersonalAccountAuthenticationToken nonPersonalAccountAuthenticationToken = new NonPersonalAccountAuthenticationToken(Credentials
                    .builder()
                    .keyId((String) parsedTokenFromBasicHeader.getPrincipal())
                    .password((String) parsedTokenFromBasicHeader.getCredentials())
                    .build(), null, null);
            SecurityContextHolder.getContext().setAuthentication(nonPersonalAccountAuthenticationToken);
        }
    }

}
