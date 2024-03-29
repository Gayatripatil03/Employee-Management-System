package com.ness.emps.utils;


import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {
	
	@Autowired 
	private UserRepository userRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	private static final long EXPIRATION_TIME = 5 * 60 * 1000; //3 min
	
	public String generateToken(String email,String role)
	{
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
		String token =  Jwts.builder()
				.setSubject(email)
				.claim("role", role)
				.setIssuedAt(now)
				.setExpiration(expiryDate)
				.signWith(secretKey)
				.compact();
		
		// Save the token in database and update the token 
		UserDtls user = userRepo.findByEmail(email);
		if(user != null)
		{
			user.setToken(token);
	        userRepo.save(user);

		}else {
			return "User not found by this email";
		}
		
		return token;
        
	}
	public String validateToken(String token) {
		try {
            Jwts.parser()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token);

		return "valid";
		}catch(ExpiredJwtException ex) {
			//Token is expired
			return "expired token";
		}catch(JwtException | IllegalArgumentException e) {
			//Token is invalid(failed parsing or verification)
			return "invalid token";
		}
	}
	
	public String validateEmailPasswordAndRole(String email, String password,String role) {
		UserDtls user = userRepo.findByEmail(email);
	    if (user != null) {
	        // Use BCryptPasswordEncoder to match the password
	        if (passwordEncoder.matches(password, user.getPassword())) {
	        	if(user.getRole().equals(role)) {
	        		return "valid";
	        	}else {
	        		return	"Invalid role for this user";
	        	}
	            
	        } else {
	            return "Invalid email and password";
	        }
	    } else {
	        return "Invalid email and password";
	    }
    }
	
	public String extractUsername(String token) {
		return Jwts.parser()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
	
	public Claims extractAllClaims(String token) {
		JwtParserBuilder jwtParserBuilder = Jwts.parser().setSigningKey(secretKey);
	    JwtParser jwtParser = jwtParserBuilder.build();
	    return jwtParser.parseClaimsJws(token).getBody();
	}

}