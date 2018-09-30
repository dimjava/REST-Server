package com.reshigo.controller.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by dmitry103 on 26/11/16.
 */
public class VersionsInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(VersionsInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {

        LinkedList<Cookie> cookies = new LinkedList<>();

        if (httpServletRequest.getCookies() == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND, "Cookie not set");

            return true;
        }


        Collections.addAll(cookies, httpServletRequest.getCookies());

        Cookie platform = null;
        Cookie version = null;

        try {
            platform = cookies.stream().filter(cookie -> cookie.getName().equals("platform")).collect(Collectors.toList()).get(0);
            version = cookies.stream().filter(cookie -> cookie.getName().equals("version")).collect(Collectors.toList()).get(0);
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND, "Cookie not set");

            return true;
        }

        Properties properties = new Properties();
        InputStream is = getClass().getClassLoader().getResourceAsStream("common.properties");

        try {
            properties.load(is);
        } catch (IOException e) {
            logger.error("Unable to load common properties", e);
        }

        if (platform.getValue().equals("ios")) {
            if (version.getValue().compareTo(properties.getProperty("ios.version")) < 0) {
                httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);

                return false;
            }

            return true;
        }

        if (platform.getValue().equals("android")) {
            if (version.getValue().compareTo(properties.getProperty("android.version")) < 0) {
                httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);

                return false;
            }

            return true;
        }

        return false;
    }
}
