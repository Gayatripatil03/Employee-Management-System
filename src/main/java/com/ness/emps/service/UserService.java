package com.ness.emps.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

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
	
	public String authenticateUser(String username, String password,String token);
	
    public String authenticate_User(String username, String password);

	public String getUserRole(String username);

	public UserDetails loadUserByUsername(String username);
	
	public boolean checkPhoneNumber(String phone);
	
    public List<UserDtls> findBYKeyword(String keyword);

    public List<UserDtls> findByDepartmentAndRole(String department, String role);
    
	public List<UserDtls> findByKeywordAndDepartment(String keyword, String department, String role);

	public void uploadProfileImage(MultipartFile file, Long userId);


}
