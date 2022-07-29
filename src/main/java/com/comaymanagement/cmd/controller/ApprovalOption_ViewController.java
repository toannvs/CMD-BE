package com.comaymanagement.cmd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.ApprovalOption_ViewService;

@RestController
@RequestMapping("/approval-option")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApprovalOption_ViewController {
	@Autowired
	ApprovalOption_ViewService approvalOption_ViewService;
	
	
	@GetMapping("")
	public ResponseEntity<Object> findAll(@RequestParam(value = "name", required = false) String name) {
		return approvalOption_ViewService.findAll(name);
	}
}
