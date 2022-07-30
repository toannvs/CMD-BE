package com.comaymanagement.cmd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.StatusService;

@RestController
@RequestMapping(value = "status")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatusController {
	
	@Autowired
	StatusService statusService;
	
	
	
	@GetMapping("/task")
	@PreAuthorize("@customRoleService.canView('task',principal)")
	public ResponseEntity<Object> findAllForTask(){
		return statusService.findAllForTask();
	}
	
	@GetMapping("/proposal")
	@PreAuthorize("@customRoleService.canView('proposal',principal)")
	public ResponseEntity<Object> findAllForProposal(){
		return statusService.findAllForProposal();
	}
	
}
