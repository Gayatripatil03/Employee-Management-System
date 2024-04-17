package com.ness.emps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ness.emps.model.UserDtls;





public interface UserRepository extends JpaRepository<UserDtls,Long>{
	public boolean existsByEmail(String email);

	public UserDtls findByEmail(String email);

	public UserDtls findByEmailAndPhone(String email,String phone);
	
	public UserDtls findByEmailAndPassword(String email,String password);
	
	public UserDtls findByPhone(String phone);
	
	public UserDtls findByEmailAndRole(String email,String role);
}
