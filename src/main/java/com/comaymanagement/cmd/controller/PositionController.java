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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.PositionService;

@RestController
@RequestMapping("/positions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PositionController {
	@Autowired
	PositionService positionService;

	@PreAuthorize("@customRoleService.canView('position',principal)")
	@GetMapping("/{roleId}")
	public ResponseEntity<Object> findAll(@PathVariable(value = "roleId", required = true) Integer roleId) {
		return positionService.findAllByRoleId(roleId);
	}
	

	@PreAuthorize("@customRoleService.canDelete('position',principal)")
	@DeleteMapping(value = "/delete/{id}")
	@ResponseBody
	public ResponseEntity<Object> deletePosition(@PathVariable String id) {
		return positionService.delete(id);
	}

	@PreAuthorize("@customRoleService.canView('position',principal)")
	@GetMapping("dep/{depId}")
	public ResponseEntity<Object> findAllByDepartmentId(@PathVariable(value = "depId", required = true) Integer depId) {
		return positionService.findAllByDepartmentId(depId);
	}

	@PreAuthorize("@customRoleService.canView('position',principal)")
	@GetMapping("team/{teamId}")
	public ResponseEntity<Object> findAllByTeamId(@PathVariable(value = "teamId", required = true) Integer teamId) {
		return positionService.findAllByDepartmentId(teamId);
	}
	
}
