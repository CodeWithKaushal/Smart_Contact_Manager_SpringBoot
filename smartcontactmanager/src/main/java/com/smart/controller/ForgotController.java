package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entites.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	

	@Autowired
	private EmailService emailService;
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	// email id form open handler

	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		System.out.println("EMAIL " + email);

		// generate otp of 6 digit
		
		// Generate a random integer between 100000 and 999999 (both inclusive)
        //return random.nextInt(900000) + 100000;
		Random random = new Random();

		int otp = random.nextInt(900000) + 100000;

		System.out.println("OTP " + otp);

		// write code for send otp to email

		String subject = "OTP FROM SCM";
		String message = ""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is "
				+ "<b>"+otp
				+ "</n>"
				+ "</h1>"
				+ "</div>";
		String to = email;

		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
			
		} else {
			session.setAttribute("message", "Check your email id....");
			return "forgot_email_form";
		}
	}

	//verify otp
	 
	@PostMapping("/verify-otp")
	public String vrifyOtp(@RequestParam("otp") int otp,HttpSession session) {
		
		int myOtp=(int)session.getAttribute("myotp");
		String email=(String)session.getAttribute("email");
		
		if(myOtp==otp) {
			//password change form
			
		User user=	this.userRepository.getUserByuserName(email);
			
			if(user==null) {
				//send error message
				session.setAttribute("message", "User does not exsit with this email....");
				return "forgot_email_form";
				
			}else {
				//send change password form
			}
			
			return "password_change_form";
		}else {
			
			session.setAttribute("message", "Invalid OTP !!!");
			return "verify_otp";
		}
		
	}
	
	
	//change password
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword,HttpSession session) {
		
		String email=(String)session.getAttribute("email");
		User user=this.userRepository.getUserByuserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		
		
		return "redirect:/signin?change=password changed successfully....";
	}
}
