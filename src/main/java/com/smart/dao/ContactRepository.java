package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

/*
 *  This repository gives all the contacts
 *  
 *  we also use pagination concept.
 */
public interface ContactRepository extends JpaRepository<Contact, Integer> {
	
	@Query("from Contact as c where c.user.id = :userId")
	// Pageable - has two informations with it
	// 1. Current page - ( we send the page number )
	// 2. Contacts per page - (in our case it is 5 )
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable );
	
	// search contacts functionality 
	// Finds all contacts containing that 'name' and that of that particular 'user'
	public List<Contact> findByNameContainingAndUser(String name, User user);
}
