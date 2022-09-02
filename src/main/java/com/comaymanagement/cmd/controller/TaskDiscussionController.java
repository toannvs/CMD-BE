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

import com.comaymanagement.cmd.service.TaskDiscussionService;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskDiscussionController {
	private static Logger LOGGER  = LoggerFactory.getLogger(TaskController.class);
	@Autowired
	TaskDiscussionService taskDiscussService;
	
	
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@GetMapping(value = "/{id}/discuss")
	public ResponseEntity<Object> findByTaskId(@PathVariable Integer id){
		return taskDiscussService.findByTaskId(id);
	}
	@PostMapping(value = "/discuss/add")
	@ResponseBody
	public ResponseEntity<Object> add(@RequestBody String json){
		return taskDiscussService.add(json);
	}
}
