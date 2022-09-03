package com.comaymanagement.cmd.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Task;
import com.comaymanagement.cmd.entity.TaskDiscussion;
import com.comaymanagement.cmd.model.TaskDiscussionModel;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.TaskDiscussionRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
@Service
@Transactional
public class TaskDiscussionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskDiscussionService.class);

	@Autowired
	TaskDiscussionRepositoryImpl taskDiscussionRepository;
	@Autowired
	EmployeeRepositoryImpl employeeRepository;
	public ResponseEntity<Object> add(String json) {
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		List<TaskDiscussionModel> taskDiscussionModels = new ArrayList<>();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String content="";
		try {
			jsonObject = jsonMapper.readTree(json);
			Integer taskId = jsonObject.get("taskId").asInt();
			content = jsonObject.get("content").asText();
			if(content.equals("")) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Nội dung không được để trống", ""));
			}
			Integer modifyBy = userDetail.getId();
			String modifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			TaskDiscussion taskDiscussion = new TaskDiscussion();
			Task task= new Task();
			task.setId(taskId);
			Employee emp = new Employee();
			emp = employeeRepository.findById(modifyBy);
			taskDiscussion.setTask(task);	
			taskDiscussion.setContent(content);
			taskDiscussion.setModifyBy(emp);
			taskDiscussion.setModifyDate(modifyDate);
			if(taskDiscussionRepository.add(taskDiscussion)>0) {
				// Response data
				taskDiscussionModels = taskDiscussionRepository.findByTaskId(taskId);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK","Thêm thảo luận thành công",taskDiscussionModels));
			}else {
				taskDiscussionModels = taskDiscussionRepository.findByTaskId(taskId);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Thêm thảo luận thất bại", taskDiscussionModels));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Thêm thảo luận thất bại", taskDiscussionModels));
		}
		
	}
	public ResponseEntity<Object> findByTaskId(Integer taskId) {
		List<TaskDiscussionModel> taskDiscussionModels = new ArrayList<>();
				taskDiscussionModels = taskDiscussionRepository.findByTaskId(taskId);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK","",taskDiscussionModels));
		
	}
}
