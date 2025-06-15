package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {

	@Bean
	public UserDetailsService getuserDetailsService() {

		return new UserDetailsServiceimpl();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getuserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

		return daoAuthenticationProvider;
	}

	// configure method

//	protected void configure(AuthenticationManager auth) throws Exception{
//		
//		 auth.authenticationProvider(authenticationProvider());
//	}

//	@Bean
//   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//       
//	
//		
//		http.authorizeHttpRequests().requestMatchers("/admin/**").hasRole("ADMIN")
//		.requestMatchers("/user/**").hasRole("USER")
//		.requestMatchers("/**").permitAll().and().formLogin().and().csrf().disable();
//		
//		http.authenticationProvider(authenticationProvider());
//		return http.build();
//    }
//	

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf().disable().authorizeHttpRequests().
		requestMatchers("/admin/**").hasRole("ADMIN")
		.requestMatchers("/user/**").hasRole("USER")
		.requestMatchers("/**")
		.permitAll()
		.anyRequest()
		.authenticated()
		.and()
		.formLogin()
		.loginPage("/signin")
		.loginProcessingUrl("/dologin")
		.defaultSuccessUrl("/user/index");
		
		
		

		http.authenticationProvider(authenticationProvider());
		return http.build();
	}

}
