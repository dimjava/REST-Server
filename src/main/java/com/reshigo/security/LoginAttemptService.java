package com.reshigo.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.dbunit.util.Base64;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmitry103 on 31/12/2017.
 */

@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPT = 50;
    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String s) throws Exception {
                        return 0;
                    }
                });
    }

    public void succeeded(String key) {
        attemptsCache.invalidate(key);
    }

    public void failed(String key) {

        if (key == null || key.equals("")) {
            return;
        }

        int attempts = 0;

        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException ignored) {}

        attempts++;
        attemptsCache.put(key, attempts);
    }

    public String getName(HttpServletRequest request) {
        String name = null;
        try {
            name = new String(Base64.decode(request.getHeader("Authorization").split(" ")[1])).split(":")[0];
        } catch (Exception ignored) {}

        return name;
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
