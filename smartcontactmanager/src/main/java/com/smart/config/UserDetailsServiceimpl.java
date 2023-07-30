package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepository;
import com.smart.entites.User;

public class UserDetailsServiceimpl implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// fetching user form database
		
	User user =	userRepository.getUserByuserName(username);
	
	if(user==null)
	{
		throw new UsernameNotFoundException("Colud not found user");
	}
	
	CustomUserDetails customUserDetails=new CustomUserDetails(user);
		return customUserDetails;
	}

}
