package com.smart.helper;

/*
 * This class helps , in carrying success or error messages from
 * controller to view and vice versa.
 */
public class Message {
	
	private String content;
	private String type;
	
	// default constructor
	public Message() {
		
	}
	
	// parameterized constructor 
	public Message(String content, String type) {
		super();
		this.content = content;
		this.type = type;
	}
	
	// getters and setters
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
