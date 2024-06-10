package com.ness.emps.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;
import com.ness.emps.service.UserService;

import java.util.Arrays;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;



@Controller
@ControllerAdvice
@RequestMapping("/user")
public class UserController {
	
		@Autowired
		private UserRepository userRepo;
		
		@Autowired
		private UserService userService;
	
		@Autowired
		private BCryptPasswordEncoder passwordEncode;
		
		Logger log = LoggerFactory.getLogger(UserController.class);
		
		
		private static final long MAX_FILE_SIZE = 1 * 1024 * 1024; 
		
		@ModelAttribute
		private void userDetails(Model m,Principal p) {
			if(p != null){
			log.info("Principal value is present");
				String email = p.getName();
				UserDtls user =  userRepo.findByEmail(email);
				if(user != null){
				    log.info("user value is present: "+user);
					m.addAttribute("user", user);
				}
			}
		}		
		
		@GetMapping("/home")
		public String home() {
			log.info("Entered into User home request");
			return "user/home";
		}
	
		@GetMapping("/changePass")
		public String loadChangePassword() 
		{
			return "user/change_password";
		}
	
	
		@PostMapping("/updatePassword")
		public String changePassword(Principal principal,
		                             @RequestParam("oldPass") String oldPass,
		                             @RequestParam("newPass") String newPass,
		                             HttpSession session) {

		    String email = principal.getName();
		    UserDtls loginUser = userRepo.findByEmail(email);

		    boolean isOldPasswordCorrect = passwordEncode.matches(oldPass, loginUser.getPassword());
		    boolean isNewPasswordDifferent = !passwordEncode.matches(newPass, loginUser.getPassword());

		    if (isOldPasswordCorrect && isNewPasswordDifferent) {
		        loginUser.setPassword(passwordEncode.encode(newPass));
		        UserDtls updatedUser = userRepo.save(loginUser);

		        if (updatedUser != null) {
		            session.setAttribute("msg", "Password changed successfully");
		            log.info("Password changed successfully");
		        } else {
		            session.setAttribute("msg", "Something went wrong on the server");
		            log.info("Something went wrong on the server");
		        }
		    } else if (!isOldPasswordCorrect) {
		        session.setAttribute("msg", "Old password is incorrect");
	            log.info("Old password is incorrect");
		    } else {
		        session.setAttribute("msg", "New password must be different from the old password");
	            log.info("New password must be different from the old password");
		    }

		    return "redirect:/user/changePass";
		}

		@RequestMapping(value="/profile")
		public String profile(Model model,Principal principal) {
			String un =principal.getName();
			model.addAttribute("employee",userRepo.findByEmail(un));
			return "user/profile";
		}
	
		@GetMapping("/employees")
	    public String getEmployees(Model model, Principal principal, @RequestParam(required = false) String keyword) {
	        String email = principal.getName();
	        UserDtls loggedInUser = userRepo.findByEmail(email);
	        
	        if (keyword != null) {
	            List<UserDtls> searchResults = userService.findBYKeyword(keyword).stream()
	                                                      .filter(user -> user.getEmail().equals(email))
	                                                      .collect(Collectors.toList());
	            model.addAttribute("listEmployees", searchResults);
	        } else {
	            model.addAttribute("listEmployees", Arrays.asList(loggedInUser));
	        }
	        
	        return "user/employee_search";
	    }
		
		@GetMapping("/employeelist")
		public String viewHomePage(Model model, Principal principal) {
		    String loggedInUsername = principal.getName();
		    UserDtls loggedInUser = userRepo.findByEmail(loggedInUsername);
		    if (loggedInUser != null) {
		        model.addAttribute("employee", loggedInUser);
	            model.addAttribute("listEmployees", Arrays.asList(loggedInUser));
		    }
		    return findPaginated(1,"id","asc",model,principal);
		}

	
		@GetMapping("/employeelist/page/{pageNo}")
		public String findPaginated(@PathVariable(value = "pageNo") int pageNo,
		        @RequestParam("sortField") String sortField,
		        @RequestParam("sortDir") String sortDir,
		        Model model,Principal principal) {
			 String email = principal.getName();
		        UserDtls employee = userRepo.findByEmail(email);
		        
		        model.addAttribute("currentPage", 1);
		        model.addAttribute("totalPages", 1);
		        model.addAttribute("totalItems", 1);
		        model.addAttribute("sortField", sortField);
		        model.addAttribute("sortDir", sortDir);
		        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		        model.addAttribute("listEmployees", Arrays.asList(employee));
		        
		        return "user/employeelist";
		}

		
		
		@GetMapping(value = "/employeelist/showNewEmployeeForm")
		public String showNewEmployeeForm(Model model,HttpSession session){

			session.setAttribute("msg", "You don't have permission to add a new employee");
			return "redirect:/user/employeelist";
		}  

		@PostMapping("/employeelist/saveEmployee")
		public String saveEmployee(@Valid @ModelAttribute("employee") UserDtls employee,BindingResult bindingResult, HttpSession session) {
			session.setAttribute("msg", "You don't have permission to add a new employee");
			return "redirect:/user/employeelist";
		}

		@GetMapping("/employeelist/showFormForUpdate/{id}")
		public String showFormForUpdate(@PathVariable (value= "id")long id,Model model,Principal principal,HttpSession session){
		    String loggedInUsername = principal.getName();
			log.info("Username of logged in user is: "+loggedInUsername);
			
			UserDtls user = userRepo.findByEmail(loggedInUsername);
			log.info("Current user object is: "+user);
			
			if (user == null) {
		        log.info("User not found for email: " + loggedInUsername);
		        session.setAttribute("msg", "User details not found.");
		        return "redirect:/user/employeelist";
		    }

			Long currentUserId = user.getId();
		    log.info("Current user id is: " + currentUserId);
		    
			UserDtls employee = userService.getEmployeeById(id);
			if (employee == null) {
		        log.info("Employee not found with ID: " + id);
		        session.setAttribute("msg", "Employee with this ID does not exist.");
		        return "redirect:/user/employeelist";
		    }
			
			if(id != (currentUserId)) {
				session.setAttribute("msg", "You don't have permission to update this employee's information.");
				return "redirect:/user/employeelist";

			}
			
			model.addAttribute("employee",employee);
			return "user/update_employee";
		} 
		
		
		@GetMapping("/employeelist/deleteEmployee/{id}")
		public String deleteEmployee(@PathVariable (value = "id")long id, HttpSession session) {
			session.setAttribute("msg", "You don't have permission to delete employee");
			return "redirect:/user/employeelist";
		}
		
		
		@PostMapping("/employeelist/showFormForUpdate/saveUpdatedEmployee/{id}")
		public String saveUpdatedEmployee(@Valid @ModelAttribute("employee") UserDtls employee,
		                                  BindingResult bindingResult,
		                                  @PathVariable("id") long id,
		                                  @RequestParam("fullName") String fullName,
		                                  @RequestParam("address") String address,
		                                  @RequestParam("email") String email,
		                                  @RequestParam("phone") String phone,
		                                  @RequestParam("birthDate") String birthDate,
		                                  Principal principal,
		                                  HttpSession session,
		                                  HttpServletResponse response,
		                                  SessionStatus sessionStatus) {

		    String loggedInEmail = principal.getName();

		    UserDtls existingEmployee = userRepo.findById(id).orElse(null);

		    if (existingEmployee == null) {
		    	
		        session.setAttribute("msg", "Employee with this ID does not exist");
		        return "redirect:/user/employeelist/showFormForUpdate/" + id; 
		    }

		    if (!loggedInEmail.equals(existingEmployee.getEmail())) {

		    	session.setAttribute("msg", "You don't have permission to update this employee's information.");
		        return "redirect:/user/employeelist/showFormForUpdate/" + id; 
		    }

		    if (bindingResult.hasErrors()) {
		    	
		        StringBuilder errorMessage = new StringBuilder("Validation errors occurred: ");
		        bindingResult.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append("; "));
		        session.setAttribute("msg", errorMessage.toString());
		        return "redirect:/user/employeelist/showFormForUpdate/" + id;
		    }
		    
		    UserDtls existingUserWithEmail = userRepo.findByEmail(employee.getEmail());
		    if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(employee.getId())) {
		        log.info("Email already exists for another user");
		        session.setAttribute("msg", "Email already exists for another user");
		        return "redirect:/user/employeelist/showFormForUpdate/" + id;
		    }	
		    
		    UserDtls phoneExists = userRepo.findByPhone(employee.getPhone());
		    if(phoneExists != null && !phoneExists.getId().equals(employee.getId())) {
		    	log.info("phoneExists result is: "+phoneExists);
		        session.setAttribute("msg", "User with same phone no already exists");
		        return "redirect:/user/employeelist/showFormForUpdate/" + id;
		    }

		    if (!existingEmployee.getEmail().equals(email)) {

		        existingEmployee.setFullName(fullName);
		        existingEmployee.setAddress(address);
		        existingEmployee.setEmail(email);
		        existingEmployee.setPhone(phone);
                existingEmployee.setBirthDate(birthDate);


		        
		        userRepo.save(existingEmployee);

		        SecurityContextHolder.clearContext();
		        sessionStatus.setComplete();
		        Cookie cookie = new Cookie("JSESSIONID", null);
		        cookie.setMaxAge(0);
		        cookie.setPath("/");
		        cookie.setHttpOnly(false);
		        response.addCookie(cookie);

		        session.setAttribute("msg", "Your email has been changed. Please log in again.");
		        return "redirect:/signin?emailChanged=true";
		    }

			    existingEmployee.setFullName(fullName);
			    existingEmployee.setAddress(address);
			    existingEmployee.setPhone(phone);
                existingEmployee.setBirthDate(birthDate);

	
			    userRepo.save(existingEmployee);
	
			    session.setAttribute("msg", "Employee information updated successfully.");
			    return "redirect:/user/employeelist/showFormForUpdate/" + id;
		}

		@PostMapping("/uploadProfileImage")
		public String uploadProfileImage(@RequestParam("file") MultipartFile file, HttpSession session, Principal principal) {
		    try {
		        log.info("Uploading profile image for user: {}", principal.getName());
		        log.info("Content Type: {}, Size: {}", file.getContentType(), file.getSize());

		        String contentType = file.getContentType();
		        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
		            session.setAttribute("msg", "Only JPEG and PNG images are allowed.");
		            return "redirect:/user/home";
		        }
		        
		        long fileSize = file.getSize();
		        if (fileSize > MAX_FILE_SIZE) {
		            session.setAttribute("msg", "File size exceeds the maximum limit. Size must be up to 1MB.");
		            return "File size limit exceeded. Please upload a file up to 1MB.";
		        }
		        
		        String email = principal.getName();
		        UserDtls user = userRepo.findByEmail(email);
		        
		        user.setProfileImage(file.getBytes());
		        userRepo.save(user);
		        
		    } catch (IOException e) {
		        session.setAttribute("msg", "Failed to upload profile image.");
		    }
		    
		    return "redirect:/user/home";
		}
		
		@ExceptionHandler(MaxUploadSizeExceededException.class)
	    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpSession session) {
	        log.error("MaxUploadSizeExceededException caught: {}", e.getMessage());
	        session.setAttribute("msg", "File size limit exceeded. Please upload a file up to 1MB.");
	        return "redirect:/user/home";
	    }
		
	    @GetMapping("/profileImage/{userId}")
	    @ResponseBody
	    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
	        UserDtls user = userRepo.findById(userId).orElse(null);
	        if (user != null && user.getProfileImage() != null) {
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.IMAGE_JPEG); 
	            return new ResponseEntity<>(user.getProfileImage(), headers, HttpStatus.OK);
	        } else {
	            try {
	                Resource resource = new ClassPathResource("/static/images/profile-user.png");
	                byte[] defaultImage = Files.readAllBytes(resource.getFile().toPath());
	                HttpHeaders headers = new HttpHeaders();
	                headers.setContentType(MediaType.IMAGE_JPEG);
	                return new ResponseEntity<>(defaultImage, headers, HttpStatus.OK);
	            } catch (IOException e) {
	                return ResponseEntity.notFound().build();
	            }
	        }
	    }

	
}

