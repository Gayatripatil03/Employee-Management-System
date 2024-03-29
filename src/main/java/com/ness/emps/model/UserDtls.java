package com.ness.emps.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserDtls {
	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "full_Name")
	private String fullName;

	@Column
	private String token;
	
	@Column
	private String email;

	@Column
	private String address;

	@Column(name = "emp_Position")
	private String empPosition;

	@Column(name = "emp_Dept")
	private String empDepartment;

	@Column(name = "emp_Salary")
	private Float empSalary;

	@Column
	private String phone;

	@Column
	private String password;

	@Column
	private String role;
	
	private String expirationTime;
	
	public UserDtls() {
		
	}
	
	public UserDtls(Long id,String fullName,String token, String email, String address, String empPosition, String empDepartment,
			Float empSalary, String phone, String password, String role,String expirationTime) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.token = token;
		this.email = email;
		this.address = address;
		this.empPosition = empPosition;
		this.empDepartment = empDepartment;
		this.empSalary = empSalary;
		this.phone = phone;
		this.password = password;
		this.role = role;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmpPosition() {
		return empPosition;
	}

	public void setEmpPosition(String empPosition) {
		this.empPosition = empPosition;
	}

	public String getEmpDepartment() {
		return empDepartment;
	}

	public void setEmpDepartment(String empDepartment) {
		this.empDepartment = empDepartment;
	}

	public Float getEmpSalary() {
		return empSalary;
	}

	public void setEmpSalary(Float empSalary) {
		this.empSalary = empSalary;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	

	


	public boolean validateFullName() {
		return fullName != null && !fullName.trim().equals("");
	}

	public boolean validateEmail() {
		// Implement email validation logic
		return email != null && !email.trim().equals("") &&
				email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	}

	public boolean validateAddress() {
		return address != null && !address.trim().equals("");
	}

	public boolean validateEmpPosition() {
		return empPosition != null && !empPosition.trim().equals("");
	}

	public boolean validateEmpDepartment() {
		return empDepartment != null && !empDepartment.trim().equals("");
	}

	public boolean validateEmpSalary() {
		return empSalary != null && empSalary > 0; // Assuming salary cannot be negative
	}

	public boolean validatePhone() {
		// Implement phone number validation logic
		return phone != null && !phone.trim().equals("") &&
				phone.matches("\\d{10}"); // Assuming phone number is a 10-digit number
	}

	public boolean validatePassword() {
		// Implement password validation logic
		return password != null && password.length() >= 12; // Assuming password should be at least 6 characters long
	}

	public boolean validateRole() {
		return role != null && !role.trim().equals("");
	}
	
	public String getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(String expirationTime) {
		this.expirationTime = expirationTime;
	}
}
