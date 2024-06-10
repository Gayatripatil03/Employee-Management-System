package com.ness.emps.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ness.emps.model.UserDtls;


public interface UserRepository extends JpaRepository<UserDtls,Long>{
	public boolean existsByEmail(String email);

	public UserDtls findByEmail(String email);
	
	public UserDtls findByEmailAndPhone(String email,String phone);
	
	public UserDtls findByPhone(String phone);
	
	public UserDtls findByEmailAndRole(String email,String role);
	
	@Query(value="select * from user_dtls u where u.full_name like %:keyword% or u.emp_dept like %:keyword% or u.emp_position like %:keyword% or u.address like %:keyword% or u.email like %:keyword% or u.emp_salary like %:keyword% or id like %:keyword%", nativeQuery=true)
	List<UserDtls> findByKeyword(@Param("keyword") String keyword);
	
	@Query("SELECT u FROM UserDtls u WHERE MONTH(u.birthDate) = :month AND DAY(u.birthDate) = :day")
	List<UserDtls> findByBirthDateMonthAndDay(@Param("month") int month, @Param("day") int day);

	@Query("SELECT u FROM UserDtls u WHERE u.joiningDate = :joiningDate")
	List<UserDtls> findByJoiningDate(@Param("joiningDate") String joiningDate);

	@Query("SELECT u FROM UserDtls u WHERE MONTH(u.joiningDate) = :month AND DAY(u.joiningDate) = :day")
    List<UserDtls> findByJoiningDateMonthAndDay(@Param("month") int month, @Param("day") int day);
	
	@Query("SELECT u FROM UserDtls u WHERE u.empDepartment = ?1 AND u.role = ?2")
    List<UserDtls> findByDepartmentAndRole(String department, String role);
	
	@Query(value = "SELECT * FROM user_dtls u WHERE (u.full_name LIKE %:keyword% OR u.emp_position LIKE %:keyword% OR u.address LIKE %:keyword% OR u.email LIKE %:keyword% OR u.emp_salary LIKE %:keyword% OR id LIKE %:keyword%) AND u.emp_dept = :department", nativeQuery = true)
    List<UserDtls> findByKeywordAndDepartment(@Param("keyword") String keyword, @Param("department") String department);

}
