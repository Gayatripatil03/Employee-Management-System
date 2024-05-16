package com.ness.emps.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;
import com.ness.emps.service.UserService;


@Controller
@RequestMapping("/admin")
public class AdminController {
	
	
		@Autowired
		private UserRepository userRepo;
		
		@Autowired
		private UserService userService;
		
		@Autowired
		private BCryptPasswordEncoder passwordEncode;
		
		Logger log = LoggerFactory.getLogger(AdminController.class);

				
		@ModelAttribute
		private void userDetails(Model m,Principal p) {
			String email = p.getName();
			UserDtls user =  userRepo.findByEmail(email);
			
			m.addAttribute("user", user);
		}
		
		@RequestMapping(value="/profile")
		public String profile(Model model,Principal principal) {
			String un =principal.getName();
			model.addAttribute("employee",userRepo.findByEmail(un));
			return "admin/profile";
		}
		
		@GetMapping("/home")
		public String home() {
			return "admin/home";
			
		}	
		 
		@GetMapping("/changePass")
		public String loadChangePassword() 
		{
			return "admin/change_password";
		}
		
		
		@PostMapping("/updatePassword")
		public String ChangePassword(Principal p,
				@RequestParam("oldPass")String oldPass,
				@RequestParam("newPass")String newPass,
				HttpSession session) 
		{
			
			String email = p.getName();
			
			UserDtls loginUser = userRepo.findByEmail(email);
			
			boolean b = passwordEncode.matches(oldPass,loginUser.getPassword());
			
			if(b) {
				loginUser.setPassword(passwordEncode.encode(newPass));
				UserDtls updatePasswordUser = userRepo.save(loginUser);
				if(updatePasswordUser != null) {
					session.setAttribute("msg", "Password change successfully");
				}else
				{
					session.setAttribute("msg", "Something wrong on server");
				}
			}else {
				session.setAttribute("msg", "Old password is wrong");
			}
			
			return "redirect:/admin/changePass";
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
			return "admin/employee_search";
		}
		
		
		@GetMapping("/employeelist")
		public String viewHomePage(Model model) {
			
				return findPaginated(1,"fullName","asc",model);
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
			return "admin/employeelist";
			
			
		}
		
		
		@GetMapping(value = "/employeelist/showNewEmployeeForm")
		public String showNewEmployeeForm(Model model){
			UserDtls employee = new UserDtls();
			model.addAttribute("employee",employee);
			return "admin/new_employee";
		}


		@PostMapping("/employeelist/saveEmployee")
		public String saveEmployee(@Valid @ModelAttribute("employee") UserDtls employee,BindingResult bindingResult, HttpSession session) {
			if (bindingResult.hasErrors()) {
				 log.info("Validation error occurs ");
	             StringBuilder errorMessage = new StringBuilder("Validation errors occurred: ");
	             log.info("There is problem for validation parameters: "+errorMessage);
	             bindingResult.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append("; "));
	             
	             if (bindingResult.getFieldError("password") != null) {
	                 errorMessage.append("Password validation failed. ");
	             }
	             
	             session.setAttribute("msg", errorMessage.toString());
	             return "redirect:/admin/employeelist/showNewEmployeeForm";
		    }
			

			UserDtls existingUser = userRepo.findByEmail(employee.getEmail());
		    if (existingUser != null) {

		    	log.info("User with this email id already exists");
		        session.setAttribute("msg", "User with this email id already exists");
		        return "redirect:/admin/employeelist/showNewEmployeeForm";
		    }
		    
		    UserDtls phoneExists = userRepo.findByPhone(employee.getEmail());
		    if(phoneExists != null) {
		    	log.info("phoneExists result is: "+phoneExists);
		        session.setAttribute("msg", "User with same phone no already exists");
		        return "redirect:/admin/employeelist/showNewEmployeeForm";
		    }
	    	


		    userService.saveEmployee(employee);
	    	log.info("Employee added successfully");
		    session.setAttribute("msg", "Employee added successfully");
		    return "redirect:/admin/employeelist/showNewEmployeeForm";
		}


		@GetMapping("/employeelist/showFormForUpdate/{id}")
		public String showFormForUpdate(@PathVariable (value= "id")long id,Model model){
			UserDtls employee = userService.getEmployeeById(id);
			model.addAttribute("employee",employee);
			return "admin/update_employee";
		} 
		
		@PostMapping("/employeelist/showFormForUpdate/saveUpdatedEmployee/{id}")
		public String saveUpdatedEmployee(@Valid @ModelAttribute("employee") UserDtls employee,BindingResult bindingResult,
		        @PathVariable("id") long id,
		        @RequestParam("fullName") String fullName,
		        @RequestParam("address") String address,
		        @RequestParam("email") String email,
		        @RequestParam("phone") String phone,
		        @RequestParam("birthDate") String birthDate,
		        @RequestParam("joiningDate") String joiningDate,
		        @RequestParam("empPosition") String empPosition,
		        @RequestParam("empDepartment") String empDepartment,
		        @RequestParam("empSalary") float empSalary,
		        @RequestParam("role") String role,
		        HttpSession session) {
			
			if (bindingResult.hasErrors()) {
				 log.info("Validation error occurs ");
	             StringBuilder errorMessage = new StringBuilder("Validation errors occurred: ");
	             log.info("There is problem for validation parameters: "+errorMessage);
	             bindingResult.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append("; "));
	             session.setAttribute("msg", errorMessage.toString());
	             return "redirect:/admin/employeelist/showFormForUpdate/"+id;
		    }

		    UserDtls existingEmployeeId = userService.getEmployeeById(id);

		    if (existingEmployeeId == null) {
		        log.info("User with this ID does not exist");
		        session.setAttribute("msg", "User with this ID does not exist");
		        return "redirect:/admin/employeelist/showFormForUpdate/"+id; 
		    }

		    UserDtls existingUserWithEmail = userRepo.findByEmail(employee.getEmail());
		    if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(employee.getId())) {
		        log.info("Email already exists for another user");
		        session.setAttribute("msg", "Email already exists for another user");
		        return "redirect:/admin/employeelist/showFormForUpdate/" + id;
		    }		    

		    existingEmployeeId.setFullName(fullName);
		    existingEmployeeId.setAddress(address);
		    existingEmployeeId.setEmail(email);
		    existingEmployeeId.setPhone(phone);
		    existingEmployeeId.setBirthDate(birthDate);
		    existingEmployeeId.setJoiningDate(joiningDate);
		    existingEmployeeId.setEmpPosition(empPosition);
		    existingEmployeeId.setEmpDepartment(empDepartment);
		    existingEmployeeId.setEmpSalary(empSalary);
		    existingEmployeeId.setRole(role);

		    userService.saveEmployee(existingEmployeeId);
		    log.info("Employee information updated successfully");
		    session.setAttribute("msg", "Employee information updated successfully");
		    return "redirect:/admin/employeelist/showFormForUpdate/"+id;		
		   
		}


		@GetMapping("/employeelist/deleteEmployee/{id}")
		public String deleteEmployee(@PathVariable (value = "id")long id, HttpSession session) {
		    UserDtls employee = this.userService.getEmployeeById(id);
		    if (employee == null) {
		        session.setAttribute("msg", "Employee with this ID does not exist");
		    } else {
		        this.userService.deleteEmployeeById(id);
		        session.setAttribute("msg", "Employee deleted successfully");
		        log.info("user with id: "+id+"deleted successfully");
		    }
		    return "redirect:/admin/employeelist";
		}	
	
}
