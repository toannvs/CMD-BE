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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.comaymanagement.cmd.service.EmployeeService;
import com.comaymanagement.cmd.service.StatusService;
import com.comaymanagement.cmd.service.TaskService;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {
	
	private static Logger LOGGER  = LoggerFactory.getLogger(TaskController.class);
	@Autowired
	TaskService taskService;
	
	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	StatusService statusService;
	
	/*
	@GetMapping("/{id}")
	public ResponseEntity<Object> findById(@PathVariable String id) {

		Optional<Task> task = taskService.findById(id);

		if (task != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "Query produce successfully: ", task));
		} else {

			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseObject("Not found", "Can not find task list", ""));
		}
	}*/
	
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@PostMapping(value= "",produces = "application/json")
	public ResponseEntity<Object> findAll(				
			@RequestBody String json, 
			@RequestParam(value="page", required = false) String page,
			@RequestParam(value="sort", required = false) String sort,
			@RequestParam(value="order", required = false) String order
			) {
		LOGGER.info("Get task list");
		return taskService.findAll(json,sort,order,page);
	}
	
	@PreAuthorize("@customRoleService.canCreate('task',principal)")
	@PostMapping("/add")
	@ResponseBody
	public ResponseEntity<Object> add(@RequestBody String json) {
		return taskService.add(json);
	}

	//Get task list by status id 
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@GetMapping(value="/status/{statusId}",produces = "application/json")
	public ResponseEntity<Object> findByStatusId(@PathVariable String statusId,
			@RequestParam(value="page",required = false) String page, 
			@RequestParam(value="sort", required = false) String sort,
			@RequestParam(value="order", required = false) String order){
		LOGGER.info("Get task list by status");
		return taskService.findByStatusId(statusId, sort, order, page);
	}
	
	//Get task list by status ids 
//	Axios not support on GET method with body param
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@PostMapping(value="/statuses",produces = "application/json")
	public ResponseEntity<Object> findByStatusIds(
			@RequestBody String json,
			@RequestParam(value="page",required = false) String page, 
			@RequestParam(value="sort", required = false) String sort,
			@RequestParam(value="order", required = false) String order){
		LOGGER.info("Get task list by status ids");
		return taskService.findByStatusIds(json, sort, order, page);
	}
	
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@GetMapping(value = "/{id}")
	public ResponseEntity<Object> findById(@PathVariable Integer id){

		return taskService.findById(id);
	}

	@PreAuthorize("@customRoleService.canDelete('task',principal)")
	@DeleteMapping(value = "/delete/{id}")	
	public ResponseEntity<Object> deleteTaskById(@PathVariable Integer id){
		
			return taskService.deleteTaskById(id);
	}
	
	@PreAuthorize("@customRoleService.canUpdate('task',principal)")
	@PutMapping(value = "/edit")
	@ResponseBody
	public ResponseEntity<Object> editTask(@RequestBody String json) {
		return taskService.edit(json);
	}
	
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@GetMapping(value="/filter")
	public ResponseEntity<Object> filter(
			@RequestParam(value="createFrom", required=false) String createFrom,
			@RequestParam(value="createTo", required=false) String createTo,
			@RequestParam(value="finishFrom", required=false) String finishFrom,
			@RequestParam(value="finishTo", required=false) String finishTo,
			@RequestParam(value="creator", required=false) String creator,
			@RequestParam(value="title", required=false) String title,
			@RequestParam(value="receiver", required=false) String receiver,
			@RequestParam(value="department", required=false) String department,
			@RequestParam(value="page",required = false) String page,
			@RequestParam(value="sort",required = false) String sort, 
			@RequestParam(value="order", required = false) String order){
		LOGGER.info("Filter tasks!");
		return taskService.filter(createFrom, createTo, finishFrom, finishTo, title, creator, receiver, department, order, page, sort);
	}
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@GetMapping(value= "/taskHis/{taskId}",produces = "application/json")
	public ResponseEntity<Object> findAll(@PathVariable(value="taskId",required = false) Integer taskId){
		return taskService.findAllHistoryByTaskID(taskId);
	}
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@PostMapping(value= "/assiged-to-me",produces = "application/json")
	public ResponseEntity<Object> findAllTaskAssigeToMe(@RequestBody String json,
			@RequestParam(value="page",required = false) String page, 
			@RequestParam(value="sort", required = false) String sort,
			@RequestParam(value="order", required = false) String order){
		return taskService.findAllTaskAssigeToMe(json, sort, order, page);
	}
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@PostMapping(value= "/created-by-me",produces = "application/json")
	public ResponseEntity<Object> findAllTaskCreatedByMe(@RequestBody String json,
			@RequestParam(value="page",required = false) String page, 
			@RequestParam(value="sort", required = false) String sort,
			@RequestParam(value="order", required = false) String order){
		return taskService.findAllTaskTaskCreatedByMe(json, sort, order, page);
	}
	
	@PreAuthorize("@customRoleService.canUpdate('task',principal)")
	@GetMapping(value = "/changeStatus/{id}")
	@ResponseBody
	public ResponseEntity<Object> changeStatus(@PathVariable String id) {
		return taskService.changeStatus(id);
	}
	
	@PreAuthorize("@customRoleService.canUpdate('task',principal)")
	@GetMapping(value = "/reopen/{id}")
	@ResponseBody
	public ResponseEntity<Object> reopen(@PathVariable String id) {
		return taskService.reopen(id);
	}
}

