package com.ness.emps.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
	//display list of employees
	public String viewHomePage(Model model) {
		return findPaginated(1,"fullName","asc",model);
	}
	
	// /user/employeelist/page/1?sortField=name&sortDir=asc
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
		
		
		//Add new Employee
		@GetMapping(value = "/employeelist/showNewEmployeeForm")
		public String showNewEmployeeForm(Model model){
			//create model attribute to bind form data.
			UserDtls employee = new UserDtls();
			model.addAttribute("employee",employee);
			return "new_employee";
		}

		//To save Employee details to database
		@PostMapping("/employeelist/saveEmployee")
		public String saveEmployee(@ModelAttribute("employee") UserDtls employee ){
			//save employee to database
			userService.saveEmployee(employee);
			return "redirect:/user/employeelist";

		}
		
		
		//To save updated employee details to database
		@PostMapping("/employeelist/showFormForUpdate/saveUpdatedEmployee")
		public String saveUpdatedEmployee(@ModelAttribute("employee") UserDtls employee) {
			//save updated employee info to database
			userService.saveEmployee(employee);
			return "redirect:/user/employeelist";
		}


		//To update Employee
		@GetMapping("/employeelist/showFormForUpdate/{id}")
		public String showFormForUpdate(@PathVariable (value= "id")long id,Model model){
			//get employee from the service.
			UserDtls employee = userService.getEmployeeById(id);
			//set employee as a model attribute to pre-populate the form
			model.addAttribute("employee",employee);
			return "update_employee";
		}
		
		
		//To delete an Employee
		@GetMapping("/employeelist/deleteEmployee/{id}")
		public String deleteEmployee(@PathVariable (value = "id")long id) {
			//call delete employee method
			this.userService.deleteEmployeeById(id);
			return "redirect:/user/employeelist";
		}
		
		
	
}

