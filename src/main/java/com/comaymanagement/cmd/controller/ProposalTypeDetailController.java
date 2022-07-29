package com.comaymanagement.cmd.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.ProposalTypeDetailService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/proposal-type-detail")
public class ProposalTypeDetailController {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired 
	ProposalTypeDetailService proposalTypeDetailService;
	
	
	@PreAuthorize("@customRoleService.canView('employee',principal) or @customRoleService.canViewAll('employee', principal)")
	@GetMapping(value = "{id}", produces = "application/json")
	public ResponseEntity<Object> findById(@PathVariable Integer id) {
		ResponseEntity<Object> result = proposalTypeDetailService.findById(id);
		return result;
	}
}
