package com.comaymanagement.cmd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.OptionService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/options")
public class OptionController {
	@Autowired
	OptionService optionService;
	
	@PreAuthorize("@customRoleService.canView('role',principal) or @customRoleService.canViewAll('role', principal)")
	@GetMapping(value = "", produces = "application/json")
	public ResponseEntity<Object> paggingAllEmployee() {
		ResponseEntity<Object> result = optionService.findAllWithPermissionDefault();
		return result;
	}
}
