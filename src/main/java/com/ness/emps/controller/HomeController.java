package com.ness.emps.controller;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
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
import com.ness.emps.service.MailNotificationService;
import com.ness.emps.service.EmailService;
import com.ness.emps.service.UserService;
import com.ness.emps.utils.JwtTokenUtil;
import javax.validation.Valid;
 




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
		private EmailService emailService;
		
		@Autowired MailNotificationService mailnotificationService;
		
		@Autowired
		private BCryptPasswordEncoder passwordEncoder;
		
		
		Logger log = LoggerFactory.getLogger(HomeController.class);
		
		@Scheduled(cron = "0 * * * * *")
		@GetMapping("/sendBirthdayEmails")
		public ResponseEntity<String> sendBirthdayEmails() {
		    boolean success = mailnotificationService.sendBirthdayEmails();
		    if (success) {
		        log.info("Birthday Mail sent successfully");
		        return ResponseEntity.ok("Birthday Mail sent successfully");
		    } else {
		        log.info("No users have a birthday today.");
		        return ResponseEntity.ok("No users have a birthday today.");
		    }
		}
		
		@Scheduled(cron = "0 * * * * *")
		@GetMapping("/sendOnboardingEmails")
		public ResponseEntity<String> sendOnboardingEmails() {
		    boolean success = mailnotificationService.sendOnboardingEmails();
		    if (success) {
		        log.info("Onboarding email sent successfully");
		        return ResponseEntity.ok("Onboarding email sent successfully");
		    } else {
		        log.info("No new employees joined today.");
		        return ResponseEntity.ok("No new employees joined today.");
		    }
		}
		
		@Scheduled(cron = "0 * * * * *")
		@GetMapping("/sendWorkAnniversaryEmails")
		public ResponseEntity<String> sendWorkAnniversaryEmails() {
		    boolean success = mailnotificationService.sendWorkAnniversaryEmails();
		    if (success) {
		        log.info("Work Anniversary email sent successfully");
		        return ResponseEntity.ok("Work Anniversary email sent successfully");
		    } else {
		        log.info("No users have a work anniversary today.");
		        return ResponseEntity.ok("No users have a work anniversary today.");
		    }
		}
		
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
	        log.warn("Logging out...");
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
	    public String login_req(@RequestParam("username") String username,
	                            @RequestParam("password") String password,
	                            Model model,
	                            HttpServletRequest request,
	                            HttpSession session) {
	        log.info("Entered into login request");

	        String encryptedToken = (String) request.getAttribute("encryptedToken");
	        if (encryptedToken == null) {
	            session.setAttribute("msg", "Encrypted token not found");
	            return "redirect:/signin?error=Encrypted token not found";
	        }

	        String decryptedToken = jwtTokenUtil.decryptToken(encryptedToken);
	        log.info("Decrypted token is: " + decryptedToken);

	        String tokenWithBearer = "Bearer " + decryptedToken;

	        String validationMessage = userService.authenticateUser(username, password, tokenWithBearer);
	        if (validationMessage.equals("Invalid username or password")) {
	            session.setAttribute("msg", "Invalid username or password");
	            session.removeAttribute("msg");
	            return "redirect:/signin?invalid=true";
	        } else if (!validationMessage.equals("valid")) {
	            session.setAttribute("msg", validationMessage);
	            session.removeAttribute("msg");
	            return "redirect:/signin";
	        } else {
	            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	            log.info("Authentication msg is: " + authentication);
	            if (authentication != null && authentication.isAuthenticated()) {
	                Object principal = authentication.getPrincipal();
	                log.info("Value of principal is: " + principal);
	                if (principal instanceof CustomUserDetails) {
	                    CustomUserDetails customUserDetails = (CustomUserDetails) principal;
	                    log.info("Value of CustomUserDetails is: " + customUserDetails);
	                    String email = customUserDetails.getUsername();
	                    log.info("Value of username is: " + email);
	                    UserDtls user = userRepo.findByEmail(email);
	                    log.info("Value of user object is: " + user);
	                    if (user != null) {
	                        model.addAttribute("user", user);
	                        String role = customUserDetails.getAuthorities().stream().findFirst().orElse(null).getAuthority();
	                        switch (role) {
	                            case "ROLE_ADMIN":
	                                log.info("Redirecting to ADMIN profile");
	                                return "redirect:/admin/home";
	                            case "ROLE_MANAGER":
	                                log.info("Redirecting to MANAGER profile");
	                                return "redirect:/manager/home";
	                            case "ROLE_USER":
	                                log.info("Redirecting to USER profile");
	                                return "redirect:/user/home";
	                            default:
	                                log.info("Redirecting to signin page again");
	                                return "redirect:/signin_req";
	                        }
	                    } else {
	                        return "redirect:/signin?error=User details not found";
	                    }
	                } else {
	                    return "redirect:/signin?error=Invalid principal object";
	                }
	            } else {
	                return "redirect:/signin?error=Principal object is null or not authenticated";
	            }
	        }
	    }

 
	    @PostMapping("/createUser")
	    public String createUser(@Valid @ModelAttribute UserDtls user, BindingResult bindingResult, HttpSession session) {
	        log.info("Entered into createUser method");
	        log.info("User values are: " + user);

	        if (bindingResult.hasErrors()) {
	            log.warn("Validation error occurs ");
	            StringBuilder errorMessage = new StringBuilder("Validation errors occurred: ");
	            log.info("There is problem for validation parameters: " + errorMessage);
	            bindingResult.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()));

	            if (bindingResult.getFieldError("password") != null) {
	                errorMessage.append("Password validation failed. ");
	            }

	            session.setAttribute("msg", errorMessage.toString());
	            return "redirect:/register";
	        }

	        boolean emailExists = userService.checkEmail(user.getEmail());
	        log.info("emailExists result is: " + emailExists);

	        boolean phoneExists = userService.checkPhoneNumber(user.getPhone());
	        log.info("phoneExists result is: " + phoneExists);

	        if (emailExists) {
	        	log.info("Email ID already exists session msg is displayed.");
	            session.setAttribute("msg", "Email ID already exists.");
	        } else if (phoneExists) {
	        	log.info("User with same phone no is already registered msg is displayed.");
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
	   
	    
	    @PostMapping("/forgotPassword")
	    public String forgotPassword(@RequestParam String email,@RequestParam String phone,HttpSession session){
	    	log.info("Email and phone received are: "+email +phone);
	    	UserDtls user = userRepo.findByEmailAndPhone(email, phone);
	    	log.info("Value of user is: "+user);
	    	if(user != null) {
	    		return "redirect:/loadResetPassword/" +user.getId();
	    	}else {
	    		log.info("Inavlid email and mobile number");
	    		session.setAttribute("msg", "Invalid email and mobile number.");
	    		return "forgot_password";
	    	}
	    	
	    }
	    
	    @GetMapping("/loadResetPassword/{id}")
	    public String loadResetPassword(@PathVariable long id,Model m,HttpSession session){
	        session.setAttribute("resetId", id);
	    	m.addAttribute("id",id);
	    	log.info("Id value is: "+id);
         	return "reset_password";
	    }

	    @PostMapping("/changePassword")
	    public String resetPassword(@RequestParam String psw, @RequestParam String cpsw, HttpSession session) {
	        Long resetid = (Long) session.getAttribute("resetId");
	        if (resetid == null) {
	        	session.setAttribute("msg", "Error resetting password. Reset ID not found.");
	            return "redirect:/loadForgotPassword";
	        }
	        log.info("Reset ID value is: " + resetid);

	        UserDtls user = userRepo.findById(resetid).orElse(null);
	        if (user == null) {
	            session.setAttribute("msg", "Error resetting password. User not found.");
	            return "redirect:/loadForgotPassword";
	        }

	        if (!psw.equals(cpsw)) {
	            session.setAttribute("msg", "New password and confirm password do not match");
	            return "redirect:/loadResetPassword/"+resetid;
	        }

	        if (passwordEncoder.matches(psw, user.getPassword())) {
	            session.setAttribute("msg", "New password must be different from the old password");
	            return "redirect:/loadResetPassword/"+resetid;
	        }

	        String encryptPsw = passwordEncoder.encode(psw);
	        user.setPassword(encryptPsw);
	        UserDtls updateUser = userRepo.save(user);

	        if (updateUser != null) {
	            session.setAttribute("msg", "Password changed successfully");
	        }
	        session.removeAttribute("resetId");
	        return "redirect:/loadForgotPassword";
	    }
 
}

