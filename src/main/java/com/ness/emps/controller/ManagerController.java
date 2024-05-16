package com.ness.emps.controller;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

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
@RequestMapping("/manager")
public class ManagerController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncode;
	
	Logger log = LoggerFactory.getLogger(ManagerController.class);

	
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
	
	@RequestMapping(value="/profile")
	public String profile(Model model,Principal principal) {
		String un =principal.getName();
		model.addAttribute("employee",userRepo.findByEmail(un));
		return "manager/profile";
	}
	
	@GetMapping("/home")
	public String home() {
		return "manager/home";
	}
	
	@GetMapping("/changePass")
	public String loadChangePassword() 
	{
		return "manager/change_password";
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
		
		return "redirect:/manager/changePass";
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
		return "manager/employee_search";
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
			return "manager/employeelist";
			
			
		}
		
		
		@GetMapping(value = "/employeelist/showNewEmployeeForm")
		public String showNewEmployeeForm(Model model,HttpSession session){
            session.setAttribute("msg", "You don't have permission to add a new employee");
			return "redirect:/manager/employeelist";
		}


		@PostMapping("/employeelist/saveEmployee")
		public String saveEmployee(@Valid @ModelAttribute("employee") UserDtls employee,BindingResult bindingResult, HttpSession session) {
			session.setAttribute("msg", "You don't have permission to add a new employee");
			return "redirect:/manager/employeelist";
		}

		 


		@GetMapping("/employeelist/showFormForUpdate/{id}")
		public String showFormForUpdate(@PathVariable(value= "id") long id, Model model, Principal principal, HttpSession session){
		    String loggedInEmail = principal.getName();
		    

		    UserDtls loggedInManager = userRepo.findByEmail(loggedInEmail);
		    
		    if (loggedInManager == null) {
		        session.setAttribute("msg", "Logged-in manager details not found.");
		        return "redirect:/manager/employeelist";
		    }

		    UserDtls employeeToUpdate = userService.getEmployeeById(id);

		    if (employeeToUpdate == null) {
		        session.setAttribute("msg", "Employee with this ID does not exist.");
		        return "redirect:/manager/employeelist";
		    }

		    if (!loggedInManager.getRole().equals("ROLE_ADMIN") && !loggedInManager.getEmail().equals(employeeToUpdate.getEmail()) && !employeeToUpdate.getRole().equals("ROLE_USER")) {
		        session.setAttribute("msg", "You don't have permission to update this employee's information.");
		        return "redirect:/manager/employeelist";
		    }
		    
		    if(employeeToUpdate.getId() == loggedInManager.getId()) {
			    model.addAttribute("employee", employeeToUpdate);
			    return "manager/manager_update_info";
		    }

		    model.addAttribute("employee", employeeToUpdate);
		    return "manager/update_employee";
		} 

		
		
		@PostMapping("/employeelist/showFormForUpdate/saveUpdatedEmployee/{id}")
		public String saveUpdatedEmployee(@Valid @ModelAttribute("employee") UserDtls updatedEmployee,
		                                  BindingResult bindingResult,
		                                  @PathVariable("id") long id,
		                                  Principal principal,
		                                  HttpSession session,
		                                  HttpServletResponse response,
		                                  SessionStatus sessionStatus) {
		    String loggedInEmail = principal.getName();

		    try {
		        UserDtls existingEmployee = userRepo.findById(id)
		                                .orElseThrow(() -> new NoSuchElementException("Employee not found for id :: " + id));

		        UserDtls loggedInManager = userRepo.findByEmail(loggedInEmail);

		        if (!loggedInManager.getRole().equals("ROLE_ADMIN") && !loggedInEmail.equals(existingEmployee.getEmail()) && !existingEmployee.getRole().equals("ROLE_USER")) {
		            session.setAttribute("msg", "You don't have permission to update this employee's information.");
		            return "redirect:/manager/employeelist/showFormForUpdate/" + id;
		        }

		        if (bindingResult.hasErrors()) {
		            StringBuilder errorMessage = new StringBuilder("Validation errors occurred: ");
		            bindingResult.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append("; "));
		            session.setAttribute("msg", errorMessage.toString());
		            return "redirect:/manager/employeelist/showFormForUpdate/" + id;
		        }

		        if (!existingEmployee.getEmail().equals(updatedEmployee.getEmail())) {
		            UserDtls existingUserWithEmail = userRepo.findByEmail(updatedEmployee.getEmail());
		            if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(existingEmployee.getId())) {
		                session.setAttribute("msg", "Email already exists for another user. Please provide another email.");
		                return "redirect:/manager/employeelist/showFormForUpdate/" + id;
		            }

		            if (existingEmployee.getEmail().equals(loggedInEmail)) {
		                existingEmployee.setFullName(updatedEmployee.getFullName());
		                existingEmployee.setAddress(updatedEmployee.getAddress());
		                existingEmployee.setEmail(updatedEmployee.getEmail());
		                existingEmployee.setPhone(updatedEmployee.getPhone());
		                existingEmployee.setBirthDate(updatedEmployee.getBirthDate());
		                
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
		        }

		        updatedEmployee.setEmpPosition(existingEmployee.getEmpPosition());
		        updatedEmployee.setEmpDepartment(existingEmployee.getEmpDepartment());
		        updatedEmployee.setEmpSalary(existingEmployee.getEmpSalary());
		        updatedEmployee.setJoiningDate(existingEmployee.getJoiningDate());

		        existingEmployee.setFullName(updatedEmployee.getFullName());
		        existingEmployee.setAddress(updatedEmployee.getAddress());
		        existingEmployee.setEmail(updatedEmployee.getEmail());
		        existingEmployee.setPhone(updatedEmployee.getPhone());
		        existingEmployee.setBirthDate(updatedEmployee.getBirthDate());


		        userRepo.save(existingEmployee);

		        session.setAttribute("msg", "Employee information updated successfully.");
		        return "redirect:/manager/employeelist/showFormForUpdate/" + id;
		    } catch (NoSuchElementException e) {
		        session.setAttribute("msg", "Employee with this ID does not exist");
		        return "redirect:/manager/employeelist/showFormForUpdate/" + id;
		    }
		}



			@GetMapping("/employeelist/deleteEmployee/{id}")
			public String deleteEmployee(@PathVariable (value = "id")long id, HttpSession session) {
				session.setAttribute("msg", "You don't have permission to delete employee");
				return "redirect:/manager/employeelist";
			}
}
