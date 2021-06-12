package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	public boolean sendEmail(String subject, String message, String to) {
		
		boolean flag = false;
		
		String host = "smtp.gmail.com";
		String from = "xyz@gmail.com"; // From email address
		
		// get the system properties 
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES : " + properties);
		
		// setting important info 
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.ssl", "true");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		
		// step 1: To get the session object 	
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication("From Email address", "From email password");
			}			
		});
		
		session.setDebug(true);
		
		// step 2: compose the message 
		MimeMessage m = new MimeMessage(session);
		
		try {
			// from email 
			m.setFrom(from);
			
			// adding recipient to mail 
			m.addRecipient(Message.RecipientType.TO	, new InternetAddress(to));
			
			// adding subject to message 
			m.setSubject(subject);
			
			// adding text to mail 
	//		m.setText(message);
			m.setContent(message, "text/html");
			
			// step 3: send mail using Transport class 
			Transport.send(m);
			
			System.out.println("Successfully sent...........");
			flag = true;
			
		}catch(Exception e) {
			e.printStackTrace();
		}

		return flag;
		
	}

}
