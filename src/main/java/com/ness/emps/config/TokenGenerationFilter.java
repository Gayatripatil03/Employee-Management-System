package com.ness.emps.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ness.emps.service.UserServiceImpl;
import com.ness.emps.utils.JwtTokenUtil;

@Component
public class TokenGenerationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserServiceImpl userService;
	private UserDetailsServiceImpl userDetailsServiceImpl;

	Logger log = LoggerFactory.getLogger(TokenGenerationFilter.class);

    public TokenGenerationFilter(JwtTokenUtil jwtTokenUtil, UserServiceImpl userService,UserDetailsServiceImpl userDetailsServiceImpl) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
		this.userDetailsServiceImpl = userDetailsServiceImpl;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

    	if (request.getRequestURI().equals("/login_req")) {

    		String username = request.getParameter("username");
            String password = request.getParameter("password");

            String validationMessage = userService.authenticate_User(username, password);
            log.info("User authentication message: " + validationMessage);
            if (!validationMessage.equals("valid")) {

            	response.sendRedirect("/signin?invalid=" + validationMessage);
                return;
            }


            String role = userService.getUserRole(username);
            log.info("User role value is: " + role);
            if (role == null) {

            	response.sendRedirect("/signin?error=Failed to fetch user role");
                return;
            }

            String generatedToken = jwtTokenUtil.generateToken(username, role);
            log.info("Generated token value is: " + generatedToken);


            String encryptedToken = jwtTokenUtil.encryptToken(generatedToken);
            log.info("Encrypted token is: " + encryptedToken);
            Cookie tokenCookie = new Cookie("JWT_TOKEN", encryptedToken);
            tokenCookie.setPath("/");
            tokenCookie.setHttpOnly(true); 
            response.addCookie(tokenCookie);
            
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("JSESSIONID")) {

                    	request.setAttribute("JSESSIONID", cookie.getValue());
                        break;
                    }
                }
            }
            

            request.setAttribute("encryptedToken", encryptedToken);
            log.info("Value of username and role is: "+username+role);
            String email = username;
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsernameAndRole(email, role);
            log.info("UserDetails are: "+userDetails);
            if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                    return;
                }
        
            request.getRequestDispatcher("/login_req").forward(request, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
