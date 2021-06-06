package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	// We place common data like , User object so that we can use them in every handler.
	// Since we are using @ModelAttribute at method level, this method runs automatically without even calling the method.
	@ModelAttribute
 	public void addCommonData(Model model, Principal principal) {
		// principal.getName( ) will give us user email here, 
		// i.e is a unique attribute we are using for spring security login
		String userName = principal.getName();
		System.out.println("IN USER_DASHBOARD USERNAME :" + userName);
		
		// get User object by using userName 
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER in userController : " + user);
		
		model.addAttribute("user", user);
	}
	
	// Now the url is : /user/index
	@RequestMapping("/index")
	public String dashboard( Model model, Principal principal) {
		
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	// Add contact form Handler 
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add contact");
		// sending an empty new object , so that we can collect the form data and initialize this object with that data.
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	// Handler for processing add-contact form
	@PostMapping("/process-contact")
	public String processContact( @Valid @ModelAttribute("contact") Contact newContact, BindingResult res ,
			@RequestParam("profileImage") MultipartFile file,
			Model model ,
			Principal principal, HttpSession session ) {
		
		try {
			if(res.hasErrors()) {
				// if we have any validation errors 
				throw new Exception();
			}
		}
		catch(Exception e) {
			
			model.addAttribute("contact", newContact);
			return "normal/add_contact_form";
		}
		
		try {
			String name = principal.getName();
			System.out.println("Current logged in user is: " + name);
			
			// Getting the user, who would like to add this contact to his contact list.
			User user = this.userRepository.getUserByUserName(name);
			
			// processing and uploading image file
			
			// checking if file is empty or not 
			if(file.isEmpty()) {
				// if file is empty
				System.out.println("File is empty");
				newContact.setImage("user.png");
			}
			else {
				// file is not empty, add it to a folder and update the name to contact 
				// if the file name is : default.png, then we append our cid to it.
				/*
				 *  In contact object, only the name of image is available.
				 */
				newContact.setImage(file.getOriginalFilename());
				
				System.out.println("File original name: " + file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				// path 
				Path path = Paths.get( saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is uploaded");
			}
			
			// Getting the List of contacts ( already existing ) and adding the new contact to the list.
			user.getContacts().add(newContact);
			
			/*
			 * For user object, we have added the contact. 
			 * 
			 * But now, we should also add user to the Contact. otherwise userid will be 'null' in contact table.
			 */
			newContact.setUser(user);
			
			// updating the user into database now.
			this.userRepository.save(user);
			
			System.out.println("CONTACT : " + newContact);
			System.out.println("Added to database..");
			
			// display success message
			Message successMssg = new Message("Added contact successfully !!", "alert-success");
			
			// since contact is added sucessfully, that form should be empty now
			model.addAttribute("contact", new Contact());
			
			session.setAttribute("message", successMssg);
			
		}catch(Exception e) {
			System.out.println("Error : " + e.getMessage());
			
			// display error message
			Message errorMssg = new Message("Something went wrong, Try again !!", "alert-danger");
			session.setAttribute("message", errorMssg);
		}
		
		return "normal/add_contact_form";
	}
	
	// Handler to show contacts 
	@GetMapping("/show-contacts/{page}")
	public String showContacts(
			@PathVariable("page") Integer currentpage,  // user is currently in this page.
			Model m, Principal principal ) {
		
		// sending title 
		m.addAttribute("title", "View Contacts");
		
		// this is current user mailid.
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		/* 
			// get current user email.
			String userName = principal.getName();
			// using the mail id, we get the User object of that email id.
			User user = this.userRepository.getUserByUserName(userName);
			
			// now with that user object, we have userId, using that we can get contacts.
			
			We can fecth contacts, using the above way. 
			But for the purpose of pagination we use ContactRepository
		*/
		
		Pageable pageable = PageRequest.of(currentpage, 3);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", currentpage);
		// also sending total number of pages.
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	// showing specific contact details 
	@GetMapping("/{cId}/contact")
	public String showContactDetail( @PathVariable("cId") Integer cId, 
			Model model, Principal principal ) {
		
		System.out.println("User has requested for specific contact with ID : " + cId);
		
		try {
			// fetching the Contact object with given cID from database. 
			Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			Contact contact = contactOptional.get();
			
			/*
			 *  Found a bug : 
			 *  	-> Users are able to view contacts which are not in their contact list, but existing in other Users contacts.
			 *  
			 *  Because, here in this handler, we did not verify the userID of user who is requesting to view contact.
			 */
			
			// checking current user, to remove seecurity bug.
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			if(user.getId() == contact.getUser().getId()) {
				model.addAttribute("title", contact.getName());
				
				model.addAttribute("contact", contact);
			}
			else {
				throw new Exception();
			}
			
		}catch(Exception e) {
			model.addAttribute("title", "Error !!");
			model.addAttribute("contact", null);
			model.addAttribute("message", new Message("Contact with that ID not found !!", "alert-danger"));
		}
		
		return "normal/contact_detail";
	}
	
	// Delete contact handler 
	@GetMapping("/delete/{cid}")
	public String deleteContact( @PathVariable("cid") Integer cId, 
			Model model, Principal principal,
			HttpSession session ) {
		
		System.out.println("Entered to delete !!");
		try { 
			
			Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			Contact contact = contactOptional.get();
			
			System.out.println(contact);
			
			// deleting contact
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			if(user.getId() == contact.getUser().getId()) {
				// If a valid user is requesting to delete his contact only
				// then only delete the contact.
				/* 
				 *  Actually, when we executed this delete method, the contact did not get deleted,
				 *  because in com.smart.entities.User we have used Cascade.ALL
				 *  
				 *  So, we are removing link between contact and user classes,
				 *  by setting contact.setUser(null);
				 *  
				 */
				System.out.println("User ID: " + user.getId() );
				System.out.println("Contact ID: " + contact.getcId());
				contact.setUser(null);
				
				// try to delete the image from /img folder also.
			
				// the below way of deleting the contact is not working.
		//		this.contactRepository.deleteById(contact.getcId());
				
				// so, we use the below way.
				// get user from principal username, we have User line 265.
				// removing it from the list, and in User class orphanRemoval is true.
				// uses equals method to remove. ( in contact class )
				user.getContacts().remove(contact);
				
				// again saving the modified user
				this.userRepository.save(user);
				
				// there is an alert-box in normal/base.html page
				session.setAttribute("message", new Message("Contact Deleted successfully !!", "alert-success"));
			}	
		}
		catch(Exception e) {
			session.setAttribute("message", new Message("Couldn't delete the contact !!", "alert-danger"));
		}
		return "redirect:/user/show-contacts/0";
	}
	
	// open update form handler 
	@PostMapping("/update-contact/{cid}")
	public String updateForm( @PathVariable("cid") Integer cid, 
			Model m) {
		
		m.addAttribute("title", "Update Contact");
		
		try {
			Contact contact = this.contactRepository.findById(cid).get();
			m.addAttribute("contact", contact);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return "normal/update_form";
	}
	
	
	// update contact handler 
	@RequestMapping(value = "/process-update" , method = RequestMethod.POST)
	public String updateHandler( @ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Model m, HttpSession session, Principal principal ) {
		
		try {
			
			// fetching old contact details 
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				
				// delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile , oldContactDetail.getImage());
				file1.delete();
				
				// update new Photo 
				File saveFile = new ClassPathResource("static/img").getFile();
				// path 
				Path path = Paths.get( saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}
			else {
				// because there is no new photo, old pic should remain
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			// contact updated.
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Updated successfully", "alert-success"));
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("CONTACT NAME : " + contact.getName());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	// user profile handler 
	@GetMapping("/profile")
	public String yourProfile( Model model) {
		
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
	// open settings handler 
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	
	// handler for changing password 
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword ,
			@RequestParam("newPassword") String newPassword ,
			Principal principal,
			HttpSession session ) {
		
		String userName = principal.getName();
		// getting user from database 
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		
		System.out.println("OLD PASSWORD : " + oldPassword);
		System.out.println("NEW PASSWORD : " + newPassword);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed !!", "alert-success"));
			
		}else {
			session.setAttribute("message", new Message("Your password is not changed !!", "alert-error"));

		}
		
		return "redirect:/user/index";
	}
} // END of USer Controller



















