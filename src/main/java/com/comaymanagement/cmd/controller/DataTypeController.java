package com.comaymanagement.cmd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.DataTypeService;

@RestController
@RequestMapping("/data-types")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DataTypeController {
	@Autowired
	DataTypeService dataTypeService;
	@GetMapping("")
	public ResponseEntity<Object> findAll() {
		return dataTypeService.findAll();
	}
}
