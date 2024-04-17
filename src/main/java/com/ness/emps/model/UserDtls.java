package com.ness.emps.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.*;


@Entity
public class UserDtls {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    @NotNull(message = "Full name is required")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Full name can only contain alphabetic characters and spaces")
	@Column(name = "full_Name")
	private String fullName;


	@Column
	private String token;
	
    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")  
	@Column
	private String email;

    @NotNull(message = "Address is required")
    @Size(min = 4, max = 30, message = "Address must be between 4 and 30 characters long")
	@Column
	private String address;

    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Employee position can only contain alphabetic characters and spaces")    
	@Column(name = "emp_Position")
	private String empPosition;

    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Employee department can only contain alphabetic characters and spaces")
	@Column(name = "emp_Dept")
	private String empDepartment;

    @DecimalMin(value = "0.01", message = "Employee salary must be greater than 0")
	@Column(name = "emp_Salary")
	private Float empSalary;

    @NotNull(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits long and contain only numbers")
	@Column
	private String phone;

    @NotNull(message = "Password is required")
	@Column
	private String password;

    @NotNull(message = "Role is required")
    @Pattern(regexp = "ROLE_(USER|ADMIN|MANAGER)", message = "Role must be ROLE_USER, ROLE_ADMIN, or ROLE_MANAGER")
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

	public String getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(String expirationTime) {
		this.expirationTime = expirationTime;
	}

	public String toString() {
		return "UserDtls [id=" + id + ", fullName=" + fullName + ", token=" + token + ", email=" + email + ", address="
				+ address + ", empPosition=" + empPosition + ", empDepartment=" + empDepartment + ", empSalary="
				+ empSalary + ", phone=" + phone + ", password=" + password + ", role=" + role + ", expirationTime="
				+ expirationTime + "]";
	}
	   
}
