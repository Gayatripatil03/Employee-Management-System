package com.ness.emps.config;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import com.ness.emps.service.UserServiceImpl;
import com.ness.emps.utils.JwtTokenUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler customSuccessHandler;
    

    @Autowired
    private UserDetailsServiceImpl userDetailsService; 
    
    
    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsServiceImpl userDetailsServiceImpl() {
        return new UserDetailsServiceImpl();
    }
    
    @Bean
    public UserServiceImpl userServiceImpl() {
        return new UserServiceImpl();
    }
    
    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil(); 
    }
    
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    
    @Bean
    public TokenGenerationFilter tokenGenerationFilter() {
        return new TokenGenerationFilter(jwtTokenUtil(),userServiceImpl(),userDetailsServiceImpl());
    } 
    

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(getPasswordEncoder());
        return authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }
   
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasRole("USER")
                .antMatchers("/manager/**").hasRole("MANAGER")
                .antMatchers("/generate_token","/login_req","/style","/validatetoken", "/", "/testdemo", "/signin", "/register", "/createUser", "/loadForgotPassword", "/forgotPassword", "/loadResetPassword/{id}",
                		"/sendEmail","/sendBirthdayEmails","/sendOnboardingEmails","/changePassword","/js/**","/css/**","/images/**").permitAll()
                .anyRequest().authenticated()
            .and()
                .formLogin()
                    .loginPage("/signin")
                    .loginProcessingUrl("/signin_req")
                    .successHandler(customSuccessHandler)
                    .permitAll()
            .and()
                .logout()
                    .logoutUrl("/logout") 
                    .logoutSuccessUrl("/signin?logout=Logout Successfully")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .addLogoutHandler((request, response, authentication) -> {
                        Cookie cookie = new Cookie("JSESSIONID", null);
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        cookie.setHttpOnly(false);
                        response.addCookie(cookie);
                    })
                    .permitAll()
            .and()
                .addFilterBefore(tokenGenerationFilter(), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    if (authException != null) {
                        response.sendRedirect("/");
                    }
                })
            .and()
                .headers()
                    .addHeaderWriter(new StaticHeadersWriter("Cache-Control", "no-cache, no-store, must-revalidate"))
                    .addHeaderWriter(new StaticHeadersWriter("Pragma", "no-cache"))
                    .addHeaderWriter(new StaticHeadersWriter("Expires", "0"));
    }
}
