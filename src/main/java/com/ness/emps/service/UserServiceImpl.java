package com.ness.emps.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ness.emps.config.CustomUserDetails;
import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;





@Service
public class UserServiceImpl implements UserService,UserDetailsService{
	@Autowired
	private UserRepository userRepo;

	public List<UserDtls> getAllEmployees() {

		return userRepo.findAll(); //returns list of all employees.
	}
	
	public void saveEmployee(UserDtls employee){
		this.userRepo.save(employee);
	}
	
	public void saveUpdatedEmployee(UserDtls employee) {
		this.userRepo.save(employee);
	}

	public UserDtls getEmployeeById(long id ){
		Optional<UserDtls> optional = userRepo.findById(id);
		UserDtls employee = null;
		if(optional.isPresent()) {
			employee = optional.get();
		}else {
			throw new RuntimeException("Employee not found for id :: "+id);
		}
		return employee;
	}


	public void deleteEmployeeById(long id) {
		this.userRepo.deleteById(id);
		
	}


	public Page<UserDtls> findPaginated(int pageNo, int pageSize,String sortField,String sortDirection) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending():
			Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageNo-1, pageSize,sort);
		return this.userRepo.findAll(pageable);
	}
	
	
	
	
	@Autowired
	private BCryptPasswordEncoder PasswordEncode;
	
	
	public UserDtls createUser(UserDtls user) {
		
		user.setPassword(PasswordEncode.encode(user.getPassword()));
		user.setRole("ROLE_USER");
		
		return userRepo.save(user);

	}


	public boolean checkEmail(String email) {
		return userRepo.existsByEmail(email);
	}

	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		UserDtls user = userRepo.findByEmail(email);
		
		if(user != null) {
			return new CustomUserDetails(user);
		}
		throw new UsernameNotFoundException("user not available");
	}


}
