package com.comaymanagement.cmd.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.RoleDetail;
import com.comaymanagement.cmd.entity.Task;
import com.comaymanagement.cmd.entity.TaskDiscussion;
import com.comaymanagement.cmd.entity.TaskReminder;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.RoleDetailModel;
import com.comaymanagement.cmd.model.RoleModel;
import com.comaymanagement.cmd.model.TaskDiscussionModel;
import com.comaymanagement.cmd.model.TaskReminderModel;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.TaskReminderRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.TaskRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
@Service
@Transactional
public class TaskReminderService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReminderService.class);
	@Autowired
	TaskReminderRepositoryImpl taskReminderRepository;
	@Autowired
	TaskRepositoryImpl taskRepository;
	@Autowired
	EmployeeRepositoryImpl employeeRepository;
	public ResponseEntity<Object> findByTaskId(Integer taskId) {
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		List<TaskReminderModel> taskReminders = new ArrayList<>();
		taskReminders = taskReminderRepository.findByTaskId(taskId,userDetail.getId());
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK","",taskReminders));
		
	}
	public ResponseEntity<Object> add(String json) {
		List<TaskReminderModel> taskReminders = new ArrayList<>();
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		JsonNode jsonRecordReminders;
		Integer taskId = -1;
		String time="";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
		long seconds = 0; 
		long minutes = 0; 
		long hour = 0; 
		long day = 0; 
		try {
			jsonObject = jsonMapper.readTree(json);
			jsonRecordReminders = jsonObject.get("reminders");
			String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime());
			
			for(JsonNode reminderNode : jsonRecordReminders) {
				
				taskId = reminderNode.get("taskId").asInt();
				time = reminderNode.get("time").asText();
				if(time.equals("")) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", "Thời gian không được để trống", ""));
				}
			   // Tính thời gian còn lại kể từ lúc tạo nhắc việc cho đến thời gian sẽ được nhắc
				try {
					Date start = format.parse(startDate);
					Date end = format.parse(time);
					long diff = end.getTime() - start.getTime();//as given
					
					day = TimeUnit.MILLISECONDS.toDays(diff); 
					hour = TimeUnit.MILLISECONDS.toHours(diff- TimeUnit.DAYS.toMillis(day) ); 
					minutes = TimeUnit.MILLISECONDS.toMinutes(diff - TimeUnit.HOURS.toMillis(hour) - TimeUnit.DAYS.toMillis(day)); 
				} catch (ParseException e) {
					LOGGER.error(e.getMessage());
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", "Lỗi tính time remaining", ""));
				}
				String timeRemaining = day + " ngày " + hour + " giờ " + minutes + " phút ";
				Task task = taskRepository.findByIdToEdit(taskId);
				Employee modifyBy = employeeRepository.findById(userDetail.getId());
				TaskReminder taskReminder = new TaskReminder();
				taskReminder.setTask(task);
				taskReminder.setModifyBy(modifyBy);
				taskReminder.setTime(time);
				taskReminder.setTimeRemaining(timeRemaining);
				if (taskReminderRepository.add(taskReminder)<0) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", "Lỗi thêm nhắc việc", taskReminder));
				}
				
			}
			
			//response data
			taskReminders = taskReminderRepository.findByTaskId(taskId, userDetail.getId());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK","Thêm nhắc việc thành công", taskReminders));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Thêm nhắc việc thất bại", taskReminders));
		}
		
	}
	public ResponseEntity<Object> edit(String json) {
		List<TaskReminderModel> taskReminders = new ArrayList<>();
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		JsonNode jsonRecordReminders;
		Integer id = -1;
		Integer taskId = -1;
		String time="";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
		long seconds = 0; 
		long minutes = 0; 
		long hour = 0; 
		long day = 0; 
		try {
			jsonObject = jsonMapper.readTree(json);
			jsonRecordReminders = jsonObject.get("reminders");
			String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime());
			
			for(JsonNode reminderNode : jsonRecordReminders) {
				id = reminderNode.get("id").asInt();
				taskId = reminderNode.get("taskId").asInt();
				time = reminderNode.get("time").asText();
				if(time.equals("")) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", "Thời gian không được để trống", ""));
				}
				// Tính thời gian còn lại kể từ lúc tạo nhắc việc cho đến thời gian sẽ được nhắc
				try {
					Date start = format.parse(startDate);
					Date end = format.parse(time);
					long diff = end.getTime() - start.getTime();//as given
					
					day = TimeUnit.MILLISECONDS.toDays(diff); 
					hour = TimeUnit.MILLISECONDS.toHours(diff- TimeUnit.DAYS.toMillis(day) ); 
					minutes = TimeUnit.MILLISECONDS.toMinutes(diff - TimeUnit.HOURS.toMillis(hour) - TimeUnit.DAYS.toMillis(day)); 
				} catch (ParseException e) {
					LOGGER.error(e.getMessage());
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", "Lỗi tính time remaining", ""));
				}
				String timeRemaining = day + " ngày " + hour + " giờ " + minutes + " phút ";
//				Task task = taskRepository.findByIdToEdit(taskId);
//				Employee modifyBy = employeeRepository.findById(userDetail.getId());
				
				TaskReminder taskReminder = taskReminderRepository.findById(id);
				taskReminder.setTime(time);
				taskReminder.setTimeRemaining(timeRemaining);
				if (taskReminderRepository.edit(taskReminder)<0) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", "Lỗi sửa nhắc việc", taskReminder));
				}
				
			}
			
			//response data
			taskReminders = taskReminderRepository.findByTaskId(taskId, userDetail.getId());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK","Sửa nhắc việc thành công", taskReminders));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Sửa nhắc việc thất bại", taskReminders));
		}
		
	}
	
	public ResponseEntity<Object> delete(Integer id) {
		Integer deleteStatus = taskReminderRepository.delete(id);
		try {
			if (deleteStatus>0) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK","Xóa nhắc việc thành công", id));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Xóa nhắc việc thất bại", id));

			}
		} catch (Exception e) {
			LOGGER.error("Has error: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}
		
}
