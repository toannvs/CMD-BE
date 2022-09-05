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
import com.comaymanagement.cmd.entity.MailSchedule;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.RoleDetail;
import com.comaymanagement.cmd.entity.Task;
import com.comaymanagement.cmd.entity.TaskDiscussion;
import com.comaymanagement.cmd.entity.TaskReminder;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.Request;
import com.comaymanagement.cmd.model.RoleDetailModel;
import com.comaymanagement.cmd.model.RoleModel;
import com.comaymanagement.cmd.model.TaskDiscussionModel;
import com.comaymanagement.cmd.model.TaskReminderModel;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.MailScheduleRepositoryImpl;
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
	@Autowired
	private MailScheduleService mailScheduleService;
	@Autowired
	MailScheduleRepositoryImpl mailScheduleRepository;
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
		JsonNode jsonRecordReminder;
		Integer taskId = -1;
		String time="";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
		long seconds = 0; 
		long minutes = 0; 
		long hour = 0; 
		long day = 0; 
		Task task = null;
		String scheduleId = "";
		try {
			jsonRecordReminder = jsonMapper.readTree(json);
			String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime());
			
				taskId = jsonRecordReminder.get("taskId").asInt();
				time = jsonRecordReminder.get("time").asText();
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
				task = taskRepository.findByIdToEdit(taskId);
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
				
				// Save schedule start 
				if(task!=null) {
					Request request = new Request();
					request.setSubject("Nhắc việc CMD - " + task.getTitle());
					StringBuilder message = new StringBuilder();
					message.append("Bạn có công việc cần hoàn thành trước " + task.getFinishDate() + ".");
					message.append("<br>");
					message.append("Nội dung: " + task.getDescription());
					request.setMessage(message.toString());
					request.setToEmail(task.getReceiver().getEmail());
					request.setUsername(userDetail.getUsername());
					request.setScheduledTime(time);
					request.setZoneId("Asia/Ho_Chi_Minh");
					scheduleId = mailScheduleService.createSchedule(request);
					System.out.println("SCHEDULE CREATED WITH ID " + scheduleId );
				}
			
				// Save schedule end
				// Update schedule for task reminder
				MailSchedule mailSchedule = mailScheduleRepository.findById(Integer.valueOf(scheduleId));
				taskReminder.setMailSchedule(mailSchedule);
				taskReminderRepository.edit(taskReminder);
			
			//response data start
			taskReminders = taskReminderRepository.findByTaskId(taskId, userDetail.getId());
			//response data end
		
			
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
		JsonNode jsonRecordReminder;
		Integer id = -1;
		Integer taskId = -1;
		String time="";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
		long seconds = 0; 
		long minutes = 0; 
		long hour = 0; 
		long day = 0; 
		Task task = null;
		try {
			jsonRecordReminder = jsonMapper.readTree(json);
			String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime());
			
				id = jsonRecordReminder.get("id").asInt();
				taskId = jsonRecordReminder.get("taskId").asInt();
				time = jsonRecordReminder.get("time").asText();
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
				//Update schedule start Request request = new Request();
				task = taskRepository.findByIdToEdit(taskId);
				Integer oldScheduleId = taskReminder.getMailSchedule().getScheduleId();
				mailScheduleService.deleteSchedule(oldScheduleId);
				Request request = new Request();
//				request.setScheduleId(taskReminder.getMailSchedule().getScheduleId());
				request.setSubject("Nhắc việc CMD - " + task.getTitle());
				StringBuilder message = new StringBuilder();
				message.append("Bạn có công việc cần hoàn thành trước " + task.getFinishDate() + ".");
				message.append("<br>");
				message.append("Nội dung: " + task.getDescription());
				request.setMessage(message.toString());
				request.setToEmail(task.getReceiver().getEmail());
				request.setUsername(userDetail.getUsername());
				request.setScheduledTime(time);
				request.setZoneId("Asia/Ho_Chi_Minh");
				String scheduleId = mailScheduleService.createSchedule(request);
				System.out.println("SCHEDULE DELETED WITH ID " + oldScheduleId + " AND SCHEDULE CREATED WITH ID " + scheduleId );
				//Update schedule end
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
		TaskReminder taskReminder = taskReminderRepository.findById(id);
		Integer scheduleId = taskReminder.getMailSchedule().getScheduleId();
		Integer deleteStatus = taskReminderRepository.delete(id);
		try {
			if (deleteStatus>0) {
				mailScheduleService.deleteSchedule(scheduleId);
				System.out.println("SCHEDULE DELETED WITH ID " + scheduleId);
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
