package com.ness.emps.controller;

import java.security.Principal;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@RequestMapping("/user")
public class UserController {
	
		@Autowired
		private UserRepository userRepo;
		
		@Autowired
		private UserService userService;
	
		@Autowired
		private BCryptPasswordEncoder passwordEncode;
		
		private static final Logger log = Logger.getLogger(HomeController.class.getName());
		
		
		@ModelAttribute
		private void userDetails(Model m,Principal p) {
			String email = p.getName();
			UserDtls user =  userRepo.findByEmail(email);
			
			m.addAttribute("user", user);
		}
	
		@GetMapping("/")
		public String home() {
			return "/home";
		}
	
		@GetMapping("/changePass")
		public String loadChangePassword() 
		{
			return "/change_password";
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
		
			return "redirect:/user/changePass";
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
			return "employeelist";
			
			
		}
		
		
		@GetMapping(value = "/employeelist/showNewEmployeeForm")
		public String showNewEmployeeForm(Model model){
			//create model attribute to bind form data.
			UserDtls employee = new UserDtls();
			model.addAttribute("employee",employee);
			return "new_employee";
		}  

		@PostMapping("/employeelist/saveEmployee")
		public String saveEmployee(@ModelAttribute("employee") UserDtls employee, HttpSession session) {
		    UserDtls existingUser = userRepo.findByEmail(employee.getEmail());
		    if (existingUser != null) {
		    	log.info("User with this email id already exists");
		        session.setAttribute("msg", "User with this email id already exists");
		        return "redirect:/user/employeelist/showNewEmployeeForm";
		    }

		    userService.saveEmployee(employee);
	    	log.info("Employee added successfully");
		    session.setAttribute("msg", "Employee added successfully");
		    return "redirect:/user/employeelist/showNewEmployeeForm";
		}

		@GetMapping("/employeelist/showFormForUpdate/{id}")
		public String showFormForUpdate(@PathVariable (value= "id")long id,Model model){
			UserDtls employee = userService.getEmployeeById(id);
			model.addAttribute("employee",employee);
			return "update_employee";
		} 
		
		@PostMapping("/employeelist/showFormForUpdate/saveUpdatedEmployee/{id}")
		public ResponseEntity<String> saveUpdatedEmployee(
		        @PathVariable("id") long id,
		        @RequestParam("fullName") String fullName,
		        @RequestParam("address") String address,
		        @RequestParam("email") String email,
		        @RequestParam("empPosition") String empPosition,
		        @RequestParam("empDepartment") String empDepartment,
		        @RequestParam("empSalary") float empSalary) {

		    UserDtls existingEmployeeId = userService.getEmployeeById(id);

		    if (existingEmployeeId == null) {
		        log.info("User with this ID does not exist");
		        return ResponseEntity.badRequest().body("User with this ID does not exist");
		    }

		    UserDtls existingEmployeeByEmail = userRepo.findByEmail(email);
		    if (existingEmployeeByEmail == null) {
		        log.info("User with this email does not exist");
		        return ResponseEntity.badRequest().body("User with this email is not present in the database");
		    }

		    if (existingEmployeeByEmail.getId() != id) {
		        log.info("Email does not match with the provided ID");
		        return ResponseEntity.badRequest().body("Provided email does not match with the provided ID");
		    }

		    existingEmployeeId.setFullName(fullName);
		    existingEmployeeId.setAddress(address);
		    existingEmployeeId.setEmail(email);
		    existingEmployeeId.setEmpPosition(empPosition);
		    existingEmployeeId.setEmpDepartment(empDepartment);
		    existingEmployeeId.setEmpSalary(empSalary);

		    userService.saveEmployee(existingEmployeeId);
		    log.info("Employee information updated successfully");
		    return ResponseEntity.ok("Employee information updated successfully");
		}

		@GetMapping("/employeelist/deleteEmployee/{id}")
				public String deleteEmployee(@PathVariable (value = "id")long id, HttpSession session) {
				    UserDtls employee = this.userService.getEmployeeById(id);
				    if (employee == null) {
				        session.setAttribute("msg", "Employee with this ID does not exist");
				    } else {
				        this.userService.deleteEmployeeById(id);
				        session.setAttribute("msg", "Employee deleted successfully");
				    }
				    return "redirect:/user/employeelist";
				}
	
}

