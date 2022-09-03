package com.comaymanagement.cmd.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.TaskDiscussionService;
import com.comaymanagement.cmd.service.TaskReminderService;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskReminderController {
	private static Logger LOGGER  = LoggerFactory.getLogger(TaskController.class);
	@Autowired
	TaskReminderService taskReminderService;
	@PreAuthorize("@customRoleService.canView('task',principal)")
	@GetMapping(value = "/{id}/reminders")
	public ResponseEntity<Object> findByTaskId(@PathVariable Integer id){
		return taskReminderService.findByTaskId(id);
	}
	@PostMapping(value = "/reminders/add")
	@ResponseBody
	public ResponseEntity<Object> add(@RequestBody String json){
		return taskReminderService.add(json);
	}
	@PutMapping(value = "/reminders/edit")
	@ResponseBody
	public ResponseEntity<Object> edit(@RequestBody String json){
		return taskReminderService.edit(json);
	}
	@DeleteMapping("reminders/delete/{id}")
	public ResponseEntity<Object> delete(
			@PathVariable Integer id) {
		return taskReminderService.delete(id);
	}
}
