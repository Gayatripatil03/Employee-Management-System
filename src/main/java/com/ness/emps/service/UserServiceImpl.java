package com.ness.emps.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ness.emps.config.CustomUserDetails;
import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;
import com.ness.emps.utils.JwtTokenUtil;


@Service
public class UserServiceImpl implements UserService,UserDetailsService{
	
		@Autowired
		private UserRepository userRepo;
		
		@Autowired
		private BCryptPasswordEncoder PasswordEncode;
	
		@Autowired
	    private JwtTokenUtil jwtTokenUtil;
		
		@Autowired
		private UserService  userService;
		
		
		Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	
	
		public List<UserDtls> getAllEmployees() {
	
			return userRepo.findAll();
		}
		
		public void saveEmployee(UserDtls employee){
			employee.setPassword(PasswordEncode.encode(employee.getPassword()));
			this.userRepo.save(employee);
		}
		
		public void saveUpdatedEmployee(UserDtls employee) {
			this.userRepo.save(employee);
		}
	
		public UserDtls getEmployeeById(long id ){
			Optional<UserDtls> optional = userRepo.findById(id);
			UserDtls employee = null;
			if(optional.isPresent()) {
				employee = optional.get();
			}else {
				throw new RuntimeException("Employee not found for id :: "+id);
			}
			return employee;
		}
	
	
		public void deleteEmployeeById(long id) {
			this.userRepo.deleteById(id);
			
		}
	
	
		public Page<UserDtls> findPaginated(int pageNo, int pageSize,String sortField,String sortDirection) {
			Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending():
				Sort.by(sortField).descending();
			
			Pageable pageable = PageRequest.of(pageNo-1, pageSize,sort);
			return this.userRepo.findAll(pageable);
		}
		
		
		public UserDtls createUser(UserDtls user) {
			
			log.info("User Details: {}"+user.toString()); 
			log.info("User Details are: "+user);
			log.info("Value for Password for user is: "+user.getPassword());
		

			user.setPassword(PasswordEncode.encode(user.getPassword()));

			
			return userRepo.save(user);
	
		}
	
		public boolean checkPhoneNumber(String phone) {
	        UserDtls existingUser = userRepo.findByPhone(phone);
	        return existingUser != null;
	    }
	
		public boolean checkEmail(String email) {
			return userRepo.existsByEmail(email);
		}
	
		public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	
			UserDtls user = userRepo.findByEmail(email);
			
			if(user != null) {
				return new CustomUserDetails(user);
			}
			throw new UsernameNotFoundException("user not available");
		}
		
		public String authenticateUser(String username, String password, String token) {
	        UserDtls user = userRepo.findByEmail(username);
	
	        if (user != null && PasswordEncode.matches(password, user.getPassword())) {
	            if (token != null && !token.isEmpty()) {
	                String tokenValidationResult = jwtTokenUtil.validateToken(token);
	                if (!tokenValidationResult.equals("valid")) {
	                    return "Invalid token";
	                }
	            } else {
	                return "Token is required for login";
	            }
	        	
	            return "valid";
	        } else {
		        log.info("Invalid username and password");
		        return "redirect:/signin?invalid=true";
	        }
	    }
	
	    public String getUserRole(String username) {
	        UserDtls user = userRepo.findByEmail(username);
	
	        if (user != null) {
	            return user.getRole();
	        } else {
	            return "ROLE_USER"; 
	        }
	    }
	    
	    public String authenticate_User(String username, String password) {
	        UserDtls user = userRepo.findByEmail(username);

	        if (user != null && PasswordEncode.matches(password, user.getPassword())) {
	            return "valid";
	        } else {
	            return "Invalid username or password";
	        }
	    }
	    
	    
	    public List<UserDtls> findBYKeyword(String keyword){
	    	return userRepo.findByKeyword(keyword);
	    }

}
