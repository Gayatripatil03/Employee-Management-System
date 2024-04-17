package com.ness.emps.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ness.emps.utils.JwtTokenUtil;

import io.jsonwebtoken.Claims;

import java.util.logging.Logger;

public class JwtAuthenticationFilter extends OncePerRequestFilter{

	private final JwtTokenUtil jwtTokenUtil;
	
	private UserDetailsServiceImpl userDetailsServiceImpl;
	
    private static final Logger log = Logger.getLogger(JwtAuthenticationFilter.class.getName());

	
	public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,UserDetailsServiceImpl userDetailsServiceImpl) {
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsServiceImpl = userDetailsServiceImpl;
	}
	
	public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
		this.jwtTokenUtil = jwtTokenUtil;
	}
	
	@Bean
    public UserDetailsServiceImpl userDetailsService() {
        return new UserDetailsServiceImpl();
    }
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {
	    final String authorizationHeader = request.getHeader("Authorization");

	    String username = null;
	    String jwt = null;

        log.info("Entered JwtAuthenticationFilter");

        log.info("AuthorizationHeader is: "+authorizationHeader);
	    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
	        jwt = authorizationHeader;
	        log.info("jwt is: "+jwt);
	        username = jwtTokenUtil.extractUsername(jwt.substring(7));
	        log.info("Username is: "+username);
	    }
	    
	    if (request.getRequestURI().equals("/login_req")) {
            if (jwt == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is required");
                return;
            }
        }  
	    

	    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	    	log.info("Entered for jwt validation");
	        String validationStatus = jwtTokenUtil.validateToken(jwt);
            log.info("Validation status of token: " + validationStatus);
	        if ("valid".equals(validationStatus)) {
	        	log.info("Entered into validstatus condition");
	            Claims claims = jwtTokenUtil.extractAllClaims(jwt);
	            log.info("Claims value are: "+claims);
	            String role = (String) claims.get("role");
	            log.info("Role of user is: "+role);

	            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsernameAndRole(username, role);
	            log.info("UserDetails are: "+userDetails);
	            if (userDetails != null) {
	                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
	                            userDetails, null, userDetails.getAuthorities());
	                    SecurityContextHolder.getContext().setAuthentication(authentication);
	                } else {
	                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
	                    return;
	                }
	            }
	        }
	    

	    filterChain.doFilter(request, response);
	}   
}   

