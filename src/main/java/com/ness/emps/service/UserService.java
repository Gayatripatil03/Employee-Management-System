package com.ness.emps.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ness.emps.model.UserDtls;


public interface UserService {
	
	List<UserDtls> getAllEmployees();
	
	void saveEmployee(UserDtls employee);
	
	void saveUpdatedEmployee(UserDtls employee);
	
	UserDtls getEmployeeById(long id);
	
	void deleteEmployeeById(long id);
	
	Page<UserDtls> findPaginated(int pageNo,int pageSize,String sortField,String sortDirection);
	
	
	public  UserDtls createUser(UserDtls user);
	
	public boolean checkEmail(String email);
}
