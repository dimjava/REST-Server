package com.reshigo.security;

import com.reshigo.model.entity.Authorities;
import com.reshigo.model.entity.User;
import com.reshigo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by dmitry103 on 31/12/2017.
 */

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userService.getByNameWithAuthorities(s);

        if (user == null || !user.isEnabled() || loginAttemptService.isBlocked(user.getName())) {
            throw new RuntimeException("Blocked");
        }

        UserDetails ud = new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                user.getAuthorities().stream().map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                        .collect(Collectors.toList())
        );

        return ud;
    }
}
