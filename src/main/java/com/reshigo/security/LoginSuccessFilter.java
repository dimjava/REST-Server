package com.reshigo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by dmitry103 on 12/03/17.
 */

@Component
public class LoginSuccessFilter extends BasicAuthenticationFilter {

    @Autowired
    private ConcurrentSkipListSet<String> aggregatedUsers;

    @Autowired
    public LoginSuccessFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
        super.onSuccessfulAuthentication(request, response, authResult);

        loginAttemptService.succeeded(loginAttemptService.getName(request));

        for (GrantedAuthority authority: authResult.getAuthorities()) {
            if (authority.getAuthority().equals("SOLVER")) {
                aggregatedUsers.add(authResult.getName());

                return;
            }
        }
    }

    @Override
    protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        super.onUnsuccessfulAuthentication(request, response, failed);

        loginAttemptService.failed(loginAttemptService.getName(request));
    }
}
