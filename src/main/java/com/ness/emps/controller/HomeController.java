package com.ness.emps.controller;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.ness.emps.config.UserDetailsServiceImpl;
import com.ness.emps.model.JwtReqRes;
import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;
import com.ness.emps.service.UserService;
import com.ness.emps.utils.JwtTokenUtil;




@Controller
public class HomeController {
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserService userService;
	
	
	@Autowired
	private UserRepository userRepo;
	
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
    
	
	@ModelAttribute
	private void userDetails(Model m,Principal p) {
		
		if(p != null) {
			String email = p.getName();
			UserDtls user =  userRepo.findByEmail(email);
			
			m.addAttribute("user", user);
		}
	}
	
    @GetMapping("/home")
    public String index(){
        return "index";
    }
    
    @PostMapping("/generate_token")
    public ResponseEntity<Object> generatetoken(@RequestBody JwtReqRes jwtReqRes)
    {
    	//validate email and password credentials
    	String validationMessage = jwtTokenUtil.validateEmailPasswordAndRole(jwtReqRes.getEmail(), jwtReqRes.getPassword(),jwtReqRes.getRole());
    	if(validationMessage.equals("valid")) {
    		//Generate token
    		String token = jwtTokenUtil.generateToken(jwtReqRes.getEmail(),jwtReqRes.getRole());
    		
    		// Update UserDtls object with token and expiration time
            UserDtls user = userRepo.findByEmail(jwtReqRes.getEmail());
            	if (user != null) {
            		user.setToken(token);
            		user.setExpirationTime("5 min"); // Or set the actual expiration time value
                
            		// Save the updated UserDtls object
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
    public ResponseEntity<Object> validatetoken(@RequestBody UserDtls userDtls){
    	return ResponseEntity.ok(jwtTokenUtil.validateToken(userDtls.getToken()));
    }
    
    
    @GetMapping("/signin")
    public String login(){
        return "login";
    } 

    @GetMapping("/register")
    public String register(){
        return "register";
    }
    
  /*  @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        RedirectAttributes redirectAttributes) {
        // Authenticate the user using Spring Security's AuthenticationManager
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // If authentication is successful, set authentication in SecurityContext
        if (authentication.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Redirect user based on their role
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/";
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
                return "redirect:/manager/";
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
                return "redirect:/user/";
            }
        }

        // If authentication fails, redirect to login page with error message
        return "redirect:/login?error=Invalid credentials";
    }   */
 
    
    @PostMapping("/createUser")
    public String createuser(@ModelAttribute UserDtls user,HttpSession session) {
    	
    	//System.out.println(user);
    	
    	boolean b = userService.checkEmail(user.getEmail());
    	if(b) {
    		session.setAttribute("msg", "Email Id already exists.");
    	}else {
    		UserDtls userDtls =  userService.createUser(user);
        	if(userDtls != null) {
        		session.setAttribute("msg", "Register Sucessfully");
        	}else {
        		session.setAttribute("msg", "Something wrong on server");
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
