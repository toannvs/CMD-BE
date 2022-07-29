package com.comaymanagement.cmd.controller;
/**
All option name
	todolist
	request
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.RoleDetailService;
import com.comaymanagement.cmd.service.RoleService;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {
	@Autowired
	RoleService roleService;
	@Autowired
	RoleDetailService roleDetailService;
	
	@PreAuthorize("@customRoleService.canView('role',principal) or @customRoleService.canViewAll('role',principal)")
	@GetMapping("")
	public ResponseEntity<Object> findAll(
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "page", required = false) String page) {
		
		return roleService.findAll(name, sort, order, page);
	}
	
	@PreAuthorize("@customRoleService.canView('role',principal) or @customRoleService.canViewAll('role',principal)")
	@GetMapping("/{id}")
	public ResponseEntity<Object> findRoleDetailByRoleId(@PathVariable Integer id) {
		return roleService.findRoleDetailByRoleId(id);
	}
	
	@PreAuthorize("@customRoleService.canCreate('role',principal)")
	@PostMapping("/add")
	@ResponseBody
	public ResponseEntity<Object> add(@RequestBody String json){
		return roleService.add(json);
	}
	
	@PreAuthorize("@customRoleService.canUpdate('role',principal)")
	@PutMapping("/edit")
	@ResponseBody
	public ResponseEntity<Object> edit(@RequestBody String json){
		return roleService.edit(json);
	}
	
	@PreAuthorize("@customRoleService.canDelete('role',principal)")
	@DeleteMapping("/delete/{id}")
	@ResponseBody
	public ResponseEntity<Object> delete(@PathVariable Integer id){
		return roleService.delete(id);
	}
}
