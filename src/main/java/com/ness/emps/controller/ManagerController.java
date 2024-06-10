package com.ness.emps.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
		public String getEmployees(Model model, @RequestParam(name = "keyword", required = false) String keyword, Principal principal) {
		    UserDtls loggedInUser = userRepo.findByEmail(principal.getName());
		    if (loggedInUser != null) {
		        String department = loggedInUser.getEmpDepartment();
		        String role = "ROLE_USER";

		        List<UserDtls> allEmployees = userService.findByDepartmentAndRole(department, role);

		        if (keyword == null || keyword.isEmpty()) {
		            allEmployees.add(loggedInUser);
		        }

		        List<UserDtls> matchingEmployees = userService.findByKeywordAndDepartment(keyword, department, role);

		        model.addAttribute("listEmployees", matchingEmployees);
		    }
		    return "manager/employee_search";
		}

		@GetMapping("/employeelist")
		public String viewHomePage(Model model, Principal p) {
		    return findPaginated(1, "fullName", "asc", model, p);
		}

		@GetMapping("/employeelist/page/{pageNo}")
		public String findPaginated(@PathVariable(value = "pageNo") int pageNo,
		                            @RequestParam("sortField") String sortField,
		                            @RequestParam("sortDir") String sortDir,
		                            Model model, Principal p) {
		    UserDtls loggedInUser = userRepo.findByEmail(p.getName());
		    if (loggedInUser != null) {
		        String department = loggedInUser.getEmpDepartment();
		        String role = "ROLE_USER";

		        List<UserDtls> allEmployees = userService.findByDepartmentAndRole(department, role);

		        allEmployees.add(loggedInUser);

		        Comparator<UserDtls> comparator = getComparator(sortField);
		        allEmployees.sort(sortDir.equals("asc") ? comparator : comparator.reversed());

		        int pageSize = 5;
		        int startIdx = (pageNo - 1) * pageSize;
		        int endIdx = Math.min(startIdx + pageSize, allEmployees.size());
		        List<UserDtls> paginatedEmployees = allEmployees.subList(startIdx, endIdx);

		        model.addAttribute("currentPage", pageNo);
		        model.addAttribute("totalPages", (int) Math.ceil((double) allEmployees.size() / pageSize));
		        model.addAttribute("totalItems", allEmployees.size());
		        model.addAttribute("sortField", sortField);
		        model.addAttribute("sortDir", sortDir);
		        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		        model.addAttribute("listEmployees", paginatedEmployees);
		    }
		    return "manager/employeelist";
		}

		private Comparator<UserDtls> getComparator(String sortField) {
		    switch (sortField) {
		        case "fullName":
		            return Comparator.comparing(UserDtls::getFullName);
		        case "address":
		            return Comparator.comparing(UserDtls::getAddress);
		        case "email":
		            return Comparator.comparing(UserDtls::getEmail);
		        case "empPosition":
		            return Comparator.comparing(UserDtls::getEmpPosition);
		        case "empDepartment":
		            return Comparator.comparing(UserDtls::getEmpDepartment);
		        case "empSalary":
		            return Comparator.comparing(UserDtls::getEmpSalary);
		        default:
		            return Comparator.comparing(UserDtls::getId);
		    }
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
		        
		        UserDtls existingUserWithEmail = userRepo.findByEmail(updatedEmployee.getEmail());
			    if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(updatedEmployee.getId())) {
			        log.info("Email already exists for another user");
			        session.setAttribute("msg", "Email already exists for another user");
			        return "redirect:/manager/employeelist/showFormForUpdate/" + id;
			    }	
			    
			    UserDtls phoneExists = userRepo.findByPhone(updatedEmployee.getPhone());
			    if(phoneExists != null && !phoneExists.getId().equals(updatedEmployee.getId())) {
			    	log.info("phoneExists result is: "+phoneExists);
			        session.setAttribute("msg", "User with same phone no already exists");
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
		
		@PostMapping("/uploadProfileImage")
		public String uploadProfileImage(@RequestParam("file") MultipartFile file, HttpSession session, Principal principal) {
		    try {
		        log.info("Uploading profile image for user: {}", principal.getName());
		        log.info("Content Type: {}, Size: {}", file.getContentType(), file.getSize());

		        String contentType = file.getContentType();
		        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
		            session.setAttribute("msg", "Only JPEG and PNG images are allowed.");
		            return "redirect:/admin/home";
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
		    
		    return "redirect:/admin/home";
		}
		
		@ExceptionHandler(MaxUploadSizeExceededException.class)
	    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpSession session) {
	        log.error("MaxUploadSizeExceededException caught: {}", e.getMessage());
	        session.setAttribute("msg", "File size limit exceeded. Please upload a file up to 1MB.");
	        return "redirect:/admin/home";
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
