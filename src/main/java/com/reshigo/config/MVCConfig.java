package com.reshigo.config;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.reshigo.controller.interceptors.VersionsInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

@Configuration
@EnableWebMvc
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan({"com.reshigo.controller.**", "com.reshigo.model.**"})
public class MVCConfig extends WebMvcConfigurerAdapter {
    // get a configured Hibernate4Module
    // here as an example with a disabled USE_TRANSIENT_ANNOTATION feature
    @Bean
    public Hibernate4Module hibernate4Module() {
        return new Hibernate4Module().disable(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION);
    }

    // create the ObjectMapper with Spring's Jackson2ObjectMapperBuilder
    // and passing the hibernate4Module to modulesToInstall()
    @Bean
    public MappingJackson2HttpMessageConverter jacksonMessageConverter(){
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                //.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modulesToInstall(hibernate4Module());
        return new MappingJackson2HttpMessageConverter(builder.build());
    }

    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jacksonMessageConverter());
        converters.add(stringHttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);

        registry.addInterceptor(new VersionsInterceptor());
    }

    @Bean
    public InternalResourceViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver irs = new InternalResourceViewResolver();

        irs.setPrefix("/WEB-INF/jsp/");
        irs.setSuffix(".jsp");

        return irs;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("**")
                .addResourceLocations("/resources/")
                .resourceChain(true)
                .addResolver(new GzipResourceResolver())
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/moderator/**")
                .allowCredentials(true)
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS", "PUT", "DELETE", "POST")
                .allowedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept",
                        "Access-Control-Request-Method", "Access-Control-Request-Headers", "error");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
