package com.ness.emps.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ness.emps.utils.JwtTokenUtil;

import io.jsonwebtoken.Claims;

public class JwtAuthenticationFilter extends OncePerRequestFilter{

	
	private final JwtTokenUtil jwtTokenUtil;
	
	private UserDetailsServiceImpl userDetailsServiceImpl;
	
	
	public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,UserDetailsServiceImpl userDetailsServiceImpl) {
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsServiceImpl = userDetailsServiceImpl;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {
	    final String authorizationHeader = request.getHeader("Authorization");

	    String username = null;
	    String jwt = null;

	    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
	        jwt = authorizationHeader.substring(7);
	        username = jwtTokenUtil.extractUsername(jwt);
	    }

	    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	        // Validate token
	        String validationStatus = jwtTokenUtil.validateToken(jwt);
	        if ("valid".equals(validationStatus)) {
	            // Extract user roles from JWT claims
	            Claims claims = jwtTokenUtil.extractAllClaims(jwt);
	            String role = (String) claims.get("role");

	            // Authenticate the user with roles
	            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsernameAndRole(username, role);
	            if (userDetails != null) {
	                // Check if the user roles match the roles in the token
	                if (userDetails.getAuthorities().contains(role) && isRoleAllowedForEndpoint(role, request.getRequestURI())) {
	                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
	                            userDetails, null, userDetails.getAuthorities());
	                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                    SecurityContextHolder.getContext().setAuthentication(authentication);
	                } else {
	                    // Role mismatch or endpoint not allowed
	                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
	                    return;
	                }
	            }
	        }
	    }

	    filterChain.doFilter(request, response);
	}

	// Method to check if the role is allowed for the requested endpoint
	private boolean isRoleAllowedForEndpoint(String role, String endpoint) {
	    // Check if the endpoint is for admin, manager, or user
	    if (endpoint.startsWith("/admin") && role.equals("ROLE_ADMIN")) {
	        return true;
	    } else if (endpoint.startsWith("/manager") && role.equals("ROLE_MANAGER")) {
	        return true;
	    } else if (endpoint.startsWith("/user") && role.equals("ROLE_USER")) {
	        return true;
	    }
	    
	    // Add more checks if you have additional roles and endpoints

	    // If no specific check matches, deny access
	    return false;
	}

}
