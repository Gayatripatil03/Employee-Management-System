package com.ness.emps.utils;


import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class JwtTokenUtil {
	
	@Autowired 
    private UserRepository userRepo;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
	Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);


    
    private static final long EXPIRATION_TIME = 5 * 60 * 1000; 
    
    private static final Key secretKey = generateSecretKey();

    private static Key generateSecretKey() {
        try {
        	
            KeyGenerator keyGen = KeyGenerator.getInstance(SignatureAlgorithm.HS256.getJcaName());
            SecretKey secretKey = keyGen.generateKey();
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }    
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        
        UserDtls user = userRepo.findByEmail(email);
        if (user != null) {
            user.setToken(token);
            userRepo.save(user);
        } else {
            return "User not found by this email";
        }
        
        return token;
    }
		public String validateToken(String token) {
			try {
				
		        if (token.startsWith("Bearer ")) {
		        	log.info("Token before removing Bearer:"+token);
		            token = token.substring(7);
		        	log.info("Token after removing Bearer:"+token); 
		        } else {
		            return "invalid token format";
		        }
		        
	            Jwts.parser()
	            .setSigningKey(secretKey)
	            .build()
	            .parseClaimsJws(token);
	            
	            log.info("Token for validation process is:"+token);
	            log.info("Token validated successfully");
			return "valid";
			}catch(ExpiredJwtException ex) {
				return "Expired token";
			}
			catch (SignatureException ex) {
		        return "Invalid token signature";
		    } catch (MalformedJwtException ex) {
		        return "Malformed token";
		    } catch(JwtException | IllegalArgumentException e) {
				return "invalid token";
			}
		}
		
		public Claims extractAllClaims(String token) {
		     try {
		    	log.info("Token value is: "+token);
		 		log.info("entered into extractAllClaims method");
		 		token = token.substring(7);
		 		JwtParserBuilder jwtParserBuilder = Jwts.parser().setSigningKey(secretKey);
		 		log.info("Value of JwtParserBuilder: " +jwtParserBuilder);
		 	    JwtParser jwtParser = jwtParserBuilder.build();
		 		log.info("Value of JwtParser: " +jwtParser);
		 	    return jwtParser.parseClaimsJws(token).getBody();
		     }catch(MalformedJwtException e) {
		         log.info("Error parsing JWT token: " + e.getMessage());
		         return null;
		     }		
		}
	
		public String validateEmailPasswordAndRole(String email, String password,String role) {
			UserDtls user = userRepo.findByEmail(email);
		    if (user != null) {
		        if (passwordEncoder.matches(password, user.getPassword())) {
		        	if(user.getRole().equals(role)) {
		        		return "valid";
		        	}else {
		        		return	"Invalid role for this user";
		        	}  
		        }else{
		            return "Invalid email and password";
		        }
		    }else{
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
		
		public String extractRole(String token) {
		    Claims claims = extractAllClaims(token);
		    return (String) claims.get("role");
		}
		
		public static String encryptToken(String token) {
	        try {

	        	SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
	            

	        	Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
	            byte[] encryptedBytes = cipher.doFinal(token.getBytes());
	            return Base64.getEncoder().encodeToString(encryptedBytes);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	    public static String decryptToken(String encryptedToken) {
	        try {

	        	SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
	            
	            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
	            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
	            return new String(decryptedBytes);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	    
	    public static String calculateExpirationTime() {

	    	return "EXPIRATION_TIME";
	    }
}