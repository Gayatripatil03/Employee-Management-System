package com.ness.emps.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;
import com.ness.emps.service.UserService;






@Controller
@RequestMapping("/user")
public class UserController {
	
		@Autowired
		private UserRepository userRepo;
		
		@Autowired
		private UserService userService;
	
		@Autowired
		private BCryptPasswordEncoder passwordEncode;
		
		Logger log = LoggerFactory.getLogger(UserController.class);
		
		
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
		        } else {
		            session.setAttribute("msg", "Something went wrong on the server");
		        }
		    } else if (!isOldPasswordCorrect) {
		        session.setAttribute("msg", "Old password is incorrect");
		    } else {
		        session.setAttribute("msg", "New password must be different from the old password");
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
		public String getEmployees(Model model,String keyword)
		{
			if(keyword != null) {
				model.addAttribute("listEmployees", userService.findBYKeyword(keyword));
			}
			else {
				model.addAttribute("listEmployees", userService.getAllEmployees());
			}
			return "user/employee_search";
		}
		
		@GetMapping("/employeelist")
		public String viewHomePage(Model model) {
			return findPaginated(1,"id","asc",model);
		}
	
		@GetMapping("/employeelist/page/{pageNo}")
		public String findPaginated(@PathVariable(value = "pageNo")int pageNo,
				@RequestParam("sortField") String sortField,
				@RequestParam("sortDir") String sortDir,
				Model model) {
			int pageSize = 5;
			
			Page<UserDtls> page = userService.findPaginated(pageNo, pageSize,sortField,sortDir);
			List<UserDtls> listEmployees = page.getContent();
			
			model.addAttribute("currentPage", pageNo);
			model.addAttribute("totalPages", page.getTotalPages());
			model.addAttribute("totalItems", page.getTotalElements());
			
			model.addAttribute("sortField", sortField);
			model.addAttribute("sortDir", sortDir);
			model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

			
			model.addAttribute("listEmployees", listEmployees);
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


		    if (!existingEmployee.getEmail().equals(email)) {
		    	
		        UserDtls existingUserWithEmail = userRepo.findByEmail(email);
		        if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(existingEmployee.getId())) {
		            session.setAttribute("msg", "Email already exists for another user. Please provide another email.");
		            return "redirect:/user/employeelist/showFormForUpdate/" + id;
		        }


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



		@GetMapping("/employeelist/deleteEmployee/{id}")
		public String deleteEmployee(@PathVariable (value = "id")long id, HttpSession session) {
			session.setAttribute("msg", "You don't have permission to delete employee");
			return "redirect:/user/employeelist";
		}
	
}

