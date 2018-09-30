package com.reshigo.config;

import com.reshigo.security.LoginSuccessFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableAspectJAutoProxy
@ComponentScan({ "com.reshigo.security.**" })
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final String DEF_USERS_BY_USERNAME = "select name, password, enabled " +
            "from users " +
            "where name = ? COLLATE Latin1_General_CS_AS";

    private final String DEF_AUTHORITIES_BY_USERNAME = "select user_name, authority " +
            "from authorities " +
            "where user_name = ? COLLATE Latin1_General_CS_AS";


    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Autowired
    private LoginSuccessFilter loginSuccessHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.httpBasic();

        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "**").authenticated();

        http.authorizeRequests()
                .antMatchers("/moderator/report/funds/check").hasAnyAuthority("ADMIN")
                .antMatchers("/moderator/*").hasAnyAuthority("MODERATOR", "ADMIN");

        http.authorizeRequests()
                .antMatchers("/chats/**").hasAnyAuthority("USER", "SOLVER");

        http.authorizeRequests()
                .antMatchers("/chats/**").hasAnyAuthority("USER", "SOLVER");

        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/payment").permitAll();

        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/subjects").hasAnyAuthority("ADMIN" )
                .antMatchers(HttpMethod.POST, "/themes").hasAnyAuthority("ADMIN")
                .antMatchers(HttpMethod.POST, "/messageTypes").hasAnyAuthority("ADMIN")
                .antMatchers("/user/registration/solver").hasAnyAuthority("ADMIN", "MODERATOR")
                .antMatchers(HttpMethod.GET, "/subjects/**").permitAll()
                .antMatchers(HttpMethod.GET, "/themes/**").permitAll()
                .antMatchers(HttpMethod.GET, "/messageTypes").permitAll();

        http.authorizeRequests()
                .antMatchers("/user/registration").permitAll()
                .antMatchers("/user/registration/verification/code").permitAll()
                .antMatchers("/user/registration/verification").permitAll()
                .antMatchers("/user/orders/draft/**").hasAnyAuthority("USER")
                .antMatchers("/user/orders/**").hasAnyAuthority("SOLVER", "USER")
                .antMatchers("/user/**").authenticated().and().addFilterAfter(new ShallowEtagHeaderFilter(), UsernamePasswordAuthenticationFilter.class);

        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/orders/**/pictures/**").hasAnyAuthority("SOLVER", "USER")
                .antMatchers("/orders/**").hasAnyAuthority("SOLVER");

        http.addFilter(loginSuccessHandler).authorizeRequests()
                .antMatchers("/websocket/**").authenticated();


    }
}
