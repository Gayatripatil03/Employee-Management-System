package com.ness.emps.config;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ness.emps.model.UserDtls;




public class CustomUserDetails implements UserDetails {

private UserDtls user;
	
	public CustomUserDetails(UserDtls user) {
		super();
		this.user = user;
	}

	
	public Collection<? extends GrantedAuthority> getAuthorities() {
		SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(user.getRole());
		return Arrays.asList(simpleGrantedAuthority);
	}


	public String getPassword() {
		return user.getPassword();
	}


	public String getUsername() {
		return user.getEmail();
	}


	public boolean isAccountNonExpired() {
		return true;
	}


	public boolean isAccountNonLocked() {
		return true;
	}


	public boolean isCredentialsNonExpired() {
		return true;
	}


	public boolean isEnabled() {
		return true;
	}

}
