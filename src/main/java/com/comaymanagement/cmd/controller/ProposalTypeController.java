package com.comaymanagement.cmd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.ProposalTypeService;

@RestController
@RequestMapping("/proposal-type")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProposalTypeController {
	@Autowired
	ProposalTypeService proposalTypeService;
	
	
	@PreAuthorize("@customRoleService.canView('proposal', principal) or @customRoleService.canViewAll('proposals', principal)")
	@GetMapping("/config")
	public ResponseEntity<Object> findAllConfig() {
		return proposalTypeService.findAllConfig();
	}
	@PreAuthorize("@customRoleService.canView('proposal', principal) or @customRoleService.canViewAll('proposals', principal)")
	@GetMapping("/permission")
	public ResponseEntity<Object> findAllWithPermission() {
		return proposalTypeService.findAllWithPermission();
	}
	@PreAuthorize("@customRoleService.canView('proposal', principal) or @customRoleService.canViewAll('proposals', principal)")
	@PutMapping("/edit")
	public ResponseEntity<Object> updateAndGantPermission(@RequestBody String json) {
		return proposalTypeService.updateAndGantPermission(json);
	}
	
}
