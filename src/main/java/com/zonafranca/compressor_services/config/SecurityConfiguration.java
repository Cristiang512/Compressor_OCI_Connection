package com.zonafranca.compressor_services.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

   @Override
   public void configure(final HttpSecurity http) throws Exception {
      http.csrf().disable();
      http.authorizeRequests().antMatchers("/**").permitAll().anyRequest().authenticated();
      http.headers().frameOptions().sameOrigin();
   }

}
