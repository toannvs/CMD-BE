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

import com.comaymanagement.cmd.service.TeamService;

@RestController
@RequestMapping("/teams")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TeamController {
	@Autowired
	TeamService teamService;
	
	@PreAuthorize("@customRoleService.canView('employee',principal) or @customRoleService.canViewAll('employee', principal) or @customRoleService.canCreate('employee',principal) or @customRoleService.canView('team',principal)")
	@GetMapping("")
	public ResponseEntity<Object> findAll(@RequestParam(value = "name", required = false) String name) {
		return teamService.findAll(name);
	}
//	
//	@PreAuthorize("@customRoleService.canView('team',principal)")
//	@GetMapping("")
//	public ResponseEntity<Object> findAllWith(@RequestParam(value = "name", required = false) String name) {
//		return teamService.findAll(name);
//	}
//	
	@PreAuthorize("@customRoleService.canCreate('team',principal)")
	@PostMapping("/add")
	public ResponseEntity<Object> add(@RequestBody String json){
		return teamService.add(json);
	}
	
	@PreAuthorize("@customRoleService.canUpdate('team',principal)")
	@PutMapping("/edit")
	public ResponseEntity<Object> edit(@RequestBody String json){
		return teamService.edit(json);
	}
	
	@PreAuthorize("@customRoleService.canDelete('team',principal)")
	@DeleteMapping(value = "/delete/{id}")
	@ResponseBody
	public ResponseEntity<Object> delete(@PathVariable Integer id){
		return teamService.delete(id);
	}
	@PreAuthorize("@customRoleService.canView('team', principal) or @customRoleService.canViewAll('team', principal)")
	@GetMapping("/{id}")
	public ResponseEntity<Object> findById(@PathVariable Integer id) {
		return teamService.findById(id);
	}
}
