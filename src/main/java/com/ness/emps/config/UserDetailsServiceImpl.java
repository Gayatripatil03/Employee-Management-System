package com.ness.emps.config;


import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;




@Service
public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepository userRepo;
	
    private static final Logger log = Logger.getLogger(JwtAuthenticationFilter.class.getName());

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		UserDtls user = userRepo.findByEmail(email);
		
		if(user != null) {
			return new CustomUserDetails(user);
		}
		throw new UsernameNotFoundException("user not available");
	}
	
	public UserDetails loadUserByUsernameAndRole(String email, String role) throws UsernameNotFoundException {
		log.info("Enter into method loadUserByUsernameAndRole ");
	    UserDtls user = userRepo.findByEmailAndRole(email, role); 
	    if (user != null) {
	        return new CustomUserDetails(user);
	    }
	    throw new UsernameNotFoundException("User not found with email: " + email + " and role: " + role);
	}

}

