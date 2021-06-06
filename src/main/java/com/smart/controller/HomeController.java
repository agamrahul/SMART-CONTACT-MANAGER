package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	// Autowiring Bcryppt password encoder 
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	// Autowiring UserRepository interface
	@Autowired
	private UserRepository userRepository;
	
	// This handler method is for home page.
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	// This handler method is for about page.
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	/*
	 *  This handler method is for signup page.This sends signup.html page as view
	 *  And an User object is also sent along with it.
	 *  
	 *  form collects it using th:object, it is used in two ways
	 *  	1. If that User object has any data in it, it will be displayed in the form fields
	 *  	2. When the user clicks on submit to register, that entire data will be in that User object.
	 *  
	 *  Here in the below handler, we collect that data using @ModelAttribute
	 */
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	/*
	 * handler for user registration.
	 * Gets data from signup.html page's registration form
	 * We collect that data using @ModelAttribute annotation 
	 * 
	 * If user registers successfully, it takes us to signup page with SUCCESS message
	 * 
	 * else , if user is not registered succesfully then it throws an exception.
	 * 		it takes us to signup page with ERROR message
	 */
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult res,
			@RequestParam(value="agreement", defaultValue = "false") boolean agreement ,
			Model model, HttpSession session) {
		
		try {
			/*
			 * Checking whether user has agreed the terms or not.
			 * if he agrees , agreement = true
			 * else agreement = false and throws an Exception
			 */
			if(!agreement) {
				System.out.println("User did not agreed for terms and conditions...");
				throw new Exception("You did not agreed for terms and conditions...");
			}
			
			// Part of server side validation code
			if(res.hasErrors()) {
				System.out.println("Validation Errors: " + res.toString());
				model.addAttribute("user", user);
				
				return "signup";
			}
			
			// setting role and enabled and image url.
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("user.png");
			
			// Getting the password from user , then we encode it and store that in database.
			// eg: if user password is 'abc' then we encode that using Bcrypt and store the encrypted password in database.
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement : " + agreement);
			System.out.println("User : " + user);
			
			// Saving the user in database.
			User result = this.userRepository.save(user);
			
			// System.out.println("The user inserted in database: \n" + result);
			
			// Now the user has registered successfully 
			// So, after that we should empty those fields of the registration form
			model.addAttribute("user", new User());
			
			// success message
			Message successMssg = new Message("Successfully Registered !! ", "alert-success");
			session.setAttribute("message", successMssg);
			
		}catch(Exception e) {
			
			// error message 
			Message errorMssg = new Message("Something went wrong !! " + e.getMessage(), "alert-danger");
			
			model.addAttribute("user", user);
			session.setAttribute("message", errorMssg);
		}
		
		return "signup";
	}
	
	// handler for custom login using spring security 
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		
		return "securityLogin";
	}
} // END of HomeController class