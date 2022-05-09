package com.scb.rider.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

  @Value("${upload.maxFileSizeMB}")
  private long maxFileUploadSize;

  @Bean
  @Profile("local | test")
  WebSecurityConfigurerAdapter noAuth() {
    return new WebSecurityConfigurerAdapter() {
      @Override
      protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions();
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/").permitAll();
      }
    };
  }

  @Bean
  @Profile("!local & !test")
  WebSecurityConfigurerAdapter auth() {
    return new WebSecurityConfigurerAdapter() {
      @Override
      protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions();
        http.csrf().disable();
        http.addFilterBefore(new RequestValidationFilter(), BasicAuthenticationFilter.class);
        http.addFilterBefore(new TokenValidationFilter(), BasicAuthenticationFilter.class);
        http.authorizeRequests().antMatchers("/").permitAll();
      }
    };
  }

  @Bean
  public MultipartResolver multipartResolver() {
    CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
     commonsMultipartResolver.setMaxUploadSize(maxFileUploadSize*1024*1024);
    return commonsMultipartResolver;
  }
}
