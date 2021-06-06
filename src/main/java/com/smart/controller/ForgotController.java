package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;
	
	@Autowired 
	private UserRepository	userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	Random random = new Random(1000);

	// email id form open handler 
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	
	// sending otp handler
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,
			HttpSession session ) {
		
		System.out.println("EMAIL : " + email);
		
		// generating otp of 4 digit 
		int otp = random.nextInt(9999999);
		System.out.println("OTP : " + otp);
		
		String subject = "OTP from Smart Contact Manager";
		String message = "<h1> OTP = " + otp + " </h1>";
		String to = email;
		
		// sending otp to mail 
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else {
			session.setAttribute("message", "Check your email ID");
			return "forgot_email_form";
		}
	}
	
	// verifies OTP sent to mail and user entered mail
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,
			HttpSession session) {
		
		System.out.println("OTP from session: " + otp);
		int myOtp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		
		if(otp == myOtp) {
			// allow to change password
			User user = this.userRepository.getUserByUserName(email);
			
			if(user == null) {
				// send error message 
				session.setAttribute("message", "User does not exist with this email ID");
				return "forgot_email_form";
			}
			
			return "password_change_form";
		}else {
			session.setAttribute("message", "You have entered wrong otp !!" );
			return "verify_otp";
		}
	}
	
	// change password 
	@PostMapping("/change-password") 
	public String changePassword(@RequestParam("newpassword") String newpassword ,
			HttpSession session) {
		
		String email = (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change = password change successfull";
	}
}
