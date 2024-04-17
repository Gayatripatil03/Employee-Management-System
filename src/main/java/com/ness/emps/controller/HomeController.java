package com.ness.emps.controller;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import com.ness.emps.config.CustomUserDetails;
import com.ness.emps.model.JwtReqRes;
import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;
import com.ness.emps.service.UserService;
import com.ness.emps.utils.JwtTokenUtil;

import javax.validation.Valid;

import java.util.logging.Logger;
 




@Controller
public class HomeController {
	
		@Autowired
		private JwtTokenUtil jwtTokenUtil;
		
		@Autowired
		private UserService userService;
	
		@Autowired
		private AuthenticationManager authenticationManager;
		
		@Autowired
		private UserRepository userRepo;
		
		
		@Autowired
		private BCryptPasswordEncoder passwordEncoder;
		
		
		private static final Logger log = Logger.getLogger(HomeController.class.getName());
	 
		
	    @GetMapping("/home")
	    public String index(){
	        return "index";
	    }
	    
	    @GetMapping("/signin")
	    public String signin() {
	          
	        return "signin_req";
	    }
	    
	    @GetMapping("/base")
	    public String base() {
	          
	        return "base";
	    }
	    
	    @GetMapping("/register")
	    public String register(){
	        return "register";
	    }
	    
	    @GetMapping("/logout")
	    public String logout(HttpServletRequest request, HttpServletResponse response, SessionStatus sessionStatus) {
	        log.info("Logging out...");
	        SecurityContextHolder.clearContext();
	        sessionStatus.setComplete(); 
	        Cookie cookie = new Cookie("JSESSIONID", null); 
	        cookie.setMaxAge(0); 
	        cookie.setPath("/"); 
	        cookie.setHttpOnly(false); 
	        response.addCookie(cookie); 
	        return "redirect:/signin?logout=Logout Successfully";
	    }
	
	    
	    @PostMapping("/generate_token")
	    public ResponseEntity<Object> generatetoken(@RequestBody JwtReqRes jwtReqRes)
	    {
	    	String validationMessage = jwtTokenUtil.validateEmailPasswordAndRole(jwtReqRes.getEmail(), jwtReqRes.getPassword(),jwtReqRes.getRole());
	    	if(validationMessage.equals("valid")) {
	    		String token = jwtTokenUtil.generateToken(jwtReqRes.getEmail(),jwtReqRes.getRole());
	    		log.info("Generated token:" +token);
	            UserDtls user = userRepo.findByEmail(jwtReqRes.getEmail());
	            	if (user != null) {
	            		user.setToken(token);
	            		user.setExpirationTime("5 min"); 
	                
	            		userRepo.save(user);
	                
	            		return ResponseEntity.ok(token);
	            		}else {
	            			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found for email: " + jwtReqRes.getEmail());
	            		}
	         	}else {
	         		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationMessage);
	            }
	    }
	    
	   @PostMapping("/validatetoken")
	    public ResponseEntity<Object> validateToken(@RequestParam("token") String token) {
	        log.info("Token value received for validation:"+token);
	        String tokenCheckResult = jwtTokenUtil.validateToken(token);
	        return ResponseEntity.ok(tokenCheckResult);
	    } 
	
	
	    @GetMapping("/testdemo")
	    public String getUserDetails(Authentication authentication) {
	        return "Logged in user: " + authentication.getName();
	    }


	    @PostMapping("/login_req")
	    public String login_req(@CurrentSecurityContext  SecurityContext context,@RequestParam("username") String username,
	                            @RequestParam("password") String password,
	                            @RequestParam(value = "token", required = false) String token,
	                            Model model,
	                            HttpServletRequest request) {
	        log.info("Entered into login request");
	        log.info("Username password and token value is: " +username +password +token);
	        SecurityContextHolder.clearContext();
	        String validationMessage = userService.authenticateUser(username, password, token);
	        if (validationMessage.equals("valid")) {
	            Authentication authentication = context.getAuthentication();
	            log.info("Authentication message is: " + authentication);
	            if (authentication != null && authentication.isAuthenticated()) {
	                Object principal = authentication.getPrincipal();
	                if (principal instanceof CustomUserDetails) {
	                    CustomUserDetails customUserDetails = (CustomUserDetails) principal;
	                    log.info("User details are: " + customUserDetails);
	                    String email = customUserDetails.getUsername(); 
	                    log.info("User email: " + email);
	                    UserDtls user = userRepo.findByEmail(email);
	                    if (user != null) {
	                        log.info("User details found: " + user);
	                        model.addAttribute("user", user); 
	                        String role = customUserDetails.getAuthorities().stream().findFirst().orElse(null).getAuthority();
	                        switch (role) {
	                            case "ROLE_ADMIN":
	                                log.info("Redirecting user to admin profile");
	                                return "admin/home";
	                            case "ROLE_MANAGER":
	                                log.info("Redirecting user to manager profile");
	                                return "manager/home";
	                            case "ROLE_USER":
	                                log.info("Redirecting user to user profile");
	                                return "user/home";
	                            default:
	                                log.info("Unknown role: " + role);
	                                return "forward:/signin";
	                        }
	                    } else {
	                        log.info("User details not found for email: " + email);
	                        return "redirect:/signin?error=User details not found";
	                    }
	                } else {
	                    log.info("Principal object is not an instance of CustomUserDetails");
	                    return "redirect:/signin?error=Invalid principal object";
	                }
	            } else {
	                log.info("Principal object is null or not authenticated");
	                return "redirect:/signin?error=Principal object is null or not authenticated";
	            }
	        } else {
	            log.info("Authentication failed: " + validationMessage);
	            model.addAttribute("error", validationMessage);
	            return signin();
	        }
	    }  
 
	    @PostMapping("/createUser")
	    public String createUser(@Valid @ModelAttribute UserDtls user,BindingResult bindingResult, HttpSession session) {
	    	log.info("Entered into createUser method");
	    	log.info("User values are: "+user);
	     
	        if (bindingResult.hasErrors()) {
	            log.info("Validation error occurs ");
	             StringBuilder errorMessage = new StringBuilder("Validation errors occurred: ");
	             log.info("There is problem for validation parameters: "+errorMessage);
	             bindingResult.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append("; "));
	             
	             if (bindingResult.getFieldError("password") != null) {
	                 errorMessage.append("Password validation failed. ");
	             }
	             
	             session.setAttribute("msg", errorMessage.toString());
	             return "redirect:/register";
	        } 
	         
	    
	    	 
	        boolean emailExists = userService.checkEmail(user.getEmail());
	    	log.info("emailExists result is: "+emailExists);
	
	        boolean phoneExists = userService.checkPhoneNumber(user.getPhone());
	    	log.info("phoneExists result is: "+phoneExists);
	
	        if (emailExists) {
	            session.setAttribute("msg", "Email ID already exists.");
	        } else if (phoneExists) {
	            session.setAttribute("msg", "User with same phone no is already registered.");
	        } else {
		    	log.info("Entered for createUser userService method");
	            UserDtls createdUser = userService.createUser(user);
		    	log.info("Completed with createUser userService method");
	            if (createdUser != null) {
	                session.setAttribute("msg", "Registered successfully.");
	            } else {
	                session.setAttribute("msg", "Something went wrong on the server.");
	            }
	        }
	        return "redirect:/register";
	    }  
	
	    @GetMapping("/loadForgotPassword")
	    public String loadForgotPassword() {
	    	return "forgot_password";
	    }
	    
	    @GetMapping("/loadResetPassword/{id}")
	    public String loadResetPassword(@PathVariable int id,Model m){
	    	m.addAttribute("id",id);
	    	return "reset_password";
	    }
	    
	    @PostMapping("/forgotPassword")
	    public String forgotPassword(@RequestParam String email,@RequestParam String phone,HttpSession session){
	    	
	    	UserDtls user = userRepo.findByEmailAndPhone(email, phone);
	    	
	    	if(user != null) {
	    		return "redirect:/loadResetPassword/" +user.getId();
	    	}else {
	    		session.setAttribute("msg","Invalid email and mobile number");
	    		return "forgot_password";
	    	}
	    	
	    }
	    
	    @PostMapping("/changePassword")
	    public String resetPassword(@RequestParam String psw,@RequestParam Long id,HttpSession session) {
	    	UserDtls user = userRepo.findById(id).get();
	    	String encryptPsw = passwordEncoder.encode(psw);
	    	user.setPassword(encryptPsw);
	    	UserDtls updateUser  = userRepo.save(user);
	    	
	    	if(updateUser != null) {
	    		session.setAttribute("msg","Password change successfully");
	    	}
	    	return "redirect:/loadForgotPassword";
	    }
}
