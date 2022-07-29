package com.comaymanagement.cmd.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.ProposalService;

import net.bytebuddy.asm.Advice.This;

/**
All option name
	task
	proposal
	type
	employee
	department
	position
	inventory
	team
All permission name
 	view
 	create
 	update
 	detele
 	view_all
 	update_all
 	delete_all
 **/
@RestController
@RequestMapping("/proposals")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProposalController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(This.class);
	
	@Autowired
	ProposalService proposalService;
	
	@PreAuthorize("@customRoleService.canViewAll('proposal',principal) or @customRoleService.canView('proposal',principal)")
	@PostMapping(value= "",produces = "application/json")
	public ResponseEntity<Object> findAllForAll(
				@RequestBody String json,
				@RequestParam(value = "sort", required = false) String sort,
				@RequestParam(value = "order", required = false) String order,
				@RequestParam(value = "page", required = false) String page){
		LOGGER.info("Find all proposals");
		
		return proposalService.findAllForAll(json,sort,order,page);
		
	}
	@PreAuthorize("@customRoleService.canViewAll('proposal',principal) or @customRoleService.canView('proposal',principal)")
	@PostMapping(value= "/approveByMe",produces = "application/json")
	public ResponseEntity<Object> findAllApproveByMe(
			@RequestBody String json,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "page", required = false) String page){
		LOGGER.info("Find all proposals");
		
		return proposalService.findAllApproveByMe(json,sort,order,page);
		
	}
	@PreAuthorize("@customRoleService.canViewAll('proposal',principal) or @customRoleService.canView('proposal',principal)")
	@PostMapping(value= "/createdByMe",produces = "application/json")
	public ResponseEntity<Object> findAllProposalCratedByMe(
			@RequestBody String json,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "page", required = false) String page){
		LOGGER.info("Find all proposals");
		return proposalService.findAllCreatedByMe(json,sort,order,page);
		
	}
	
	@PreAuthorize("@customRoleService.canViewAll('proposal',principal) or @customRoleService.canView('proposal',principal)")
	@GetMapping(value= "/{id}",produces = "application/json")
	public ResponseEntity<Object> findById(@PathVariable String id){
		LOGGER.info("Find proposal by id");
		return proposalService.findById(Integer.valueOf(id));
		
	}
	@PreAuthorize("@customRoleService.canCreate('proposal',principal)")
	@PostMapping(value= "/add",produces = "application/json")
	@ResponseBody
	public ResponseEntity<Object> add(@RequestBody String json){
		LOGGER.info("Add proposal");
		return proposalService.add(json);
		
	}
	
}
