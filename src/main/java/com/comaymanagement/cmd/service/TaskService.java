package com.comaymanagement.cmd.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.constant.CMDConstrant;
import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Notify;
import com.comaymanagement.cmd.entity.Pagination;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Status;
import com.comaymanagement.cmd.entity.Task;
import com.comaymanagement.cmd.entity.TaskHis;
import com.comaymanagement.cmd.model.NotifyModel;
import com.comaymanagement.cmd.model.StatusModel;
import com.comaymanagement.cmd.model.TaskModel;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.NotifyRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.StatusRepositotyImpl;
import com.comaymanagement.cmd.repositoryimpl.TaskHistoryRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.TaskRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class TaskService {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TaskRepositoryImpl taskRepository;

	@Autowired
	EmployeeRepositoryImpl employeeRepositoryImpl;

	@Autowired
	StatusRepositotyImpl statusRepositotyImpl;

	@Autowired
	TaskHistoryRepositoryImpl taskHistoryRepositoryImpl;

	@Autowired
	NotifyRepositoryImpl notifyRepositoryImpl;

	@Autowired
	Message message;

	public ResponseEntity<Object> findByStatusId(String statusId, String sort, String order, String page) {
		order = order == null ? "DESC" : order;
		sort = sort == null ? "id" : sort;
		page = page == null ? "1" : page;
		Integer limit = CMDConstrant.LIMIT;
		int offset = (Integer.valueOf(page) - 1) * limit;
		Map<String, Object> results = new TreeMap<String, Object>();
		try {
			List<TaskModel> tasksByStatusId = taskRepository.findByStatusId(statusId, sort, order, offset, limit);
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			// pagination.setTotalItem(taskRepository.CountTotalItemTaskAll());
			results.put("tasks", tasksByStatusId);
			results.put("pagination", pagination);
			if (tasksByStatusId == null) {
				LOGGER.info("Have no task by status_id: " + statusId);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("", "Have no task by status_id: " + statusId, results));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully:", results));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseObject("ERROR", e.getMessage(),results));
		}

	}

	public ResponseEntity<Object> findAll(String json, String sort, String order, String page) {
		JsonNode jsonObjectTask = null;
		JsonMapper jsonMapper = new JsonMapper();
		order = order == null || order == "" ? "DESC" : order;
		sort = sort == null || sort == "" ? "id" : sort;
		page = ((page == null) || (page == "")) ? "1" : page.trim();
		Integer limit = CMDConstrant.LIMIT;
		// Caculator offset
		String title = null, startDate = null, finishDate = null, priority = null, rate = null;
		List<TaskModel> taskModelResult = null;
		List<TaskModel> taskModelListTMP = null;
		List<Integer> statusIds = new ArrayList<Integer>();
		List<Integer> creators = new ArrayList<Integer>();
		List<Integer> receivers = new ArrayList<Integer>();
		List<Integer> departmentIds = new ArrayList<Integer>();
		Map<String, Object> results = new TreeMap<String, Object>();
		try {
			taskRepository.ScanOverDueTask();
			int offset = (Integer.valueOf(page) - 1) * limit;
			jsonObjectTask = jsonMapper.readTree(json);
			title = ((jsonObjectTask.get("title") == null) || (jsonObjectTask.get("title").asText() == "")) ? ""
					: jsonObjectTask.get("title").asText();
			startDate = ((jsonObjectTask.get("startDate") == null) || (jsonObjectTask.get("startDate").asText() == ""))
					? ""
					: jsonObjectTask.get("startDate").asText();
			finishDate = ((jsonObjectTask.get("finishDate") == null)
					|| (jsonObjectTask.get("finishDate").asText() == "")) ? ""
							: jsonObjectTask.get("finishDate").asText();
			priority = ((jsonObjectTask.get("priority") == null) || (jsonObjectTask.get("priority").asText() == ""))
					? ""
					: jsonObjectTask.get("priority").asText();
			rate = ((jsonObjectTask.get("rate") == null) || (jsonObjectTask.get("rate").asText() == "")) ? ""
					: jsonObjectTask.get("rate").asText();

			for (JsonNode jsonNode : jsonObjectTask.get("statusIds")) {
				statusIds.add(jsonNode.asInt());
			}
			for (JsonNode jsonNode : jsonObjectTask.get("creatorIds")) {
				creators.add(jsonNode.asInt());
			}
			for (JsonNode jsonNode : jsonObjectTask.get("receiverIds")) {
				receivers.add(jsonNode.asInt());
			}
			for (JsonNode jsonNode : jsonObjectTask.get("departmentIds")) {
				departmentIds.add(jsonNode.asInt());
			}
			Integer totalItem = taskRepository.countAllPaging(departmentIds, title, statusIds, creators, receivers,
					startDate, finishDate, priority, rate, sort, order, offset, limit);
			taskModelListTMP = taskRepository.findAll(departmentIds, title, statusIds, creators, receivers, startDate,
					finishDate, priority, rate, sort, order, offset, limit);
			taskModelResult = new ArrayList<TaskModel>();
			if (departmentIds.size() > 0) {
				for (int i = offset; i < totalItem; i++) {
					if (taskModelResult.size() >= limit) {
						break;
					}
					taskModelResult.add(taskModelListTMP.get(i));
				}
			} else {
				taskModelResult = taskModelListTMP;
			}
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(totalItem);
			
			results.put("pagination", pagination);
			results.put("tasks", taskModelResult);
			if (results.size() > 0) {
				// Count by status
				List<StatusModel> statusModels = taskRepository.countTaskByStatus();
				results.put("countByStatuses", statusModels);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));
			}
		} catch (Exception e) {
			LOGGER.error("ERROR:" + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject("ERROR", e.getMessage(), results));
		}

	}

	public ResponseEntity<Object> findByStatusIds(String json, String sort, String order, String page) {
		order = order == null ? "DESC" : order;
		sort = sort == null ? "id" : sort;
		page = page == null ? "1" : page.trim();
		Integer limit = CMDConstrant.LIMIT;
		int offset = (Integer.valueOf(page) - 1) * limit;
		List<TaskModel> tasks = new ArrayList<TaskModel>();
		try {

			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObject;
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			List<Integer> ids = new ArrayList<Integer>();
			for (JsonNode statusId : jsonStatusObject) {
				ids.add(Integer.valueOf(statusId.toString()));
			}

			tasks = taskRepository.findByStatusIds(ids, sort, order, offset, limit);

			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(tasks.size());
			Map<String, Object> results = new TreeMap<String, Object>();
			results.put("tasks", tasks);
			results.put("pagination", pagination);

			if (tasks.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Not found", "Can not find task list", results));
			}
		} catch (Exception e) {
			LOGGER.error("ERROR:" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> add(String json) {
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectTask;
		Status status = null;
		try {
			jsonObjectTask = jsonMapper.readTree(json);
			Task task = new Task();
			Integer receiverId = jsonObjectTask.get("receiverId") != null ? jsonObjectTask.get("receiverId").asInt()
					: -1;
//			Integer creatorId = jsonObjectTask.get("creatorId") != null ? jsonObjectTask.get("creatorId").asInt() : -1;
			String title = jsonObjectTask.get("title") != null ? jsonObjectTask.get("title").asText() : "";
			String description = jsonObjectTask.get("description") != null ? jsonObjectTask.get("description").asText()
					: "";
			String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			String modifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			String finishDate = jsonObjectTask.get("finishDate") != null ? jsonObjectTask.get("finishDate").asText()
					: "";
			Integer rate = jsonObjectTask.get("rate") != null ? jsonObjectTask.get("rate").asInt() : 1;
			Integer priority = jsonObjectTask.get("priority") != null ? jsonObjectTask.get("priority").asInt() : 1;
			String startDate = jsonObjectTask.get("startDate") != null ? jsonObjectTask.get("startDate").asText() : "";
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			Employee creator = employeeRepositoryImpl.findById(userDetail.getId());
			if (creator == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("NOT FOUND", message.getMessageByItemCode("EMPE8"), ""));
			}
			Employee receiver = employeeRepositoryImpl.findById(receiverId);
			if (receiver == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("NOT FOUND", message.getMessageByItemCode("EMPE8"), ""));
			}

			
			
			DateTimeFormatter dtf = null;
			LocalDate now = LocalDate.now();
			dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate dt = LocalDate.parse(finishDate, dtf);
			if(now.isAfter(dt)) {
				status = statusRepositotyImpl.findByIndexAndType(6, "task");
			}else {
				status = statusRepositotyImpl.findByIndexAndType(1,"task");
				if (status == null) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("NOT FOUND", message.getMessageByItemCode("STAE1"), ""));
				}
			}
			task.setCreator(creator);
			task.setReceiver(receiver);
			task.setStatus(status);
			task.setTitle(title);
			task.setDescription(description);
			task.setCreateDate(createDate);
			task.setFinishDate(finishDate);
			task.setModifyDate(modifyDate);
			task.setStartDate(startDate);
			task.setRate(rate);
			task.setPriority(priority);

			TaskModel taskModel = taskRepository.add(task);
			if (null != taskModel) {
				Notify notify = new Notify();
				notify.setIsRead(false);
				notify.setReceiver(receiver);
				notify.setTitle(message.getMessageByItemCode("TASKN2"));
				notify.setDescription(message.getMessageByItemCode("TASKN1") + CMDConstrant.SPACE + creator.getName());
				notify.setType("task");
				notify.setDetailId(task.getId());
				notifyRepositoryImpl.add(notify);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("TASKS1"), taskModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TASKE1"), ""));
			}

		} catch (Exception e) {
			LOGGER.debug("ERROR", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> findById(Integer id) {
		TaskModel taskModel = new TaskModel();
		try {
			taskModel = taskRepository.findById(id);
			if (id != null) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "SUCCESSFULLY: ", taskModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "NOT FOUND", ""));
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.debug("ERROR", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", "Have error:", e.getMessage()));

		}
	}

	//
//	public ResponseEntity<Object> sortByStatusIds(Integer statusId,String page) {
//		page = page == null ? "1" : page.trim();
//		List<taskModelAll> tasks = new ArrayList<taskModelAll>();
//		try {
//			tasks = taskRepository.sortByStatusIds(statusId, page);
//			int limit = 15;
//			Pagination pagination = new Pagination();
//			pagination.setLimit(limit);
//			pagination.setPage(Integer.valueOf(page));
//			Map<String, Object> results = new TreeMap<String, Object>();
//			results.put("pagination", pagination);
//			results.put("tasks", tasks);
//			if (results.size() > 0) {
//				return ResponseEntity.status(HttpStatus.OK)
//						.body(new ResponseObject("OK", "Query successfully: ", results));
//			} else {
//				return ResponseEntity.status(HttpStatus.NOT_FOUND)
//						.body(new ResponseObject("Not found", "Can not find task list", tasks));
//			}
//		} catch (Exception e) {
//			LOGGER.error("ERROR:" + e.getMessage());
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject("ERROR", e.getMessage(), ""));
//		}
//
//	}
	// delete task by id
	public ResponseEntity<Object> deleteTaskById(Integer id) {
		String updateStatus = taskRepository.deleteTaskById(id);
		try {
			if (updateStatus.equals("1")) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("TASKS4"), id));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TASKE2"), id));

			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", "Have error:", e.getMessage()));
		}
	}

	//
	public ResponseEntity<Object> edit(String json) {
		Task task = null;
		TaskModel taskModel = new TaskModel();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectTask;
		String messageCode = "";
		Notify notify = null;
		Status status = null;
		try {
			jsonObjectTask = jsonMapper.readTree(json);
			Integer id = jsonObjectTask.get("id").asInt();
			Integer statusId = jsonObjectTask.get("statusId") != null ? jsonObjectTask.get("statusId").asInt() : -1;
			Integer receiverId = jsonObjectTask.get("receiverId") != null ? jsonObjectTask.get("receiverId").asInt()
					: -1;
//				Integer creatorId = jsonObjectTask.get("creatorId") != null ? jsonObjectTask.get("creatorId").asInt() : -1;
			String title = jsonObjectTask.get("title") != null ? jsonObjectTask.get("title").asText() : "";
			String description = jsonObjectTask.get("description") != null ? jsonObjectTask.get("description").asText()
					: "";
			String createDate = jsonObjectTask.get("createDate") != null ? jsonObjectTask.get("createDate").asText()
					: "";
			;
			String modifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			String finishDate = jsonObjectTask.get("finishDate") != null ? jsonObjectTask.get("finishDate").asText()
					: "";
			Integer rate = jsonObjectTask.get("rate") != null ? jsonObjectTask.get("rate").asInt() : 1;
			Integer priority = jsonObjectTask.get("priority") != null ? jsonObjectTask.get("priority").asInt() : 1;
			String startDate = jsonObjectTask.get("startDate") != null ? jsonObjectTask.get("startDate").asText() : "";
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			Employee creator = employeeRepositoryImpl.findById(userDetail.getId());
			if (creator == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("NOT FOUND", message.getMessageByItemCode("EMPE8"), ""));
			}
			Employee receiver = employeeRepositoryImpl.findById(receiverId);
			if (receiver == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("NOT FOUND", message.getMessageByItemCode("EMPE8"), ""));
			}


			// statusId 3 = Đã hủy
			if (statusId == 3) {
				messageCode = "TASKS3";
			} else {
				messageCode = "TASKS2";
			}
			task = taskRepository.findByIdToEdit(id);
			if (task == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Error", message.getMessageByItemCode("TASKE3"), taskModel));
			}
			if (task.getReceiver().getId() != receiverId) {
				notify = new Notify();
				notify.setReceiver(receiver);
				notify.setIsRead(false);
				notify.setTitle(message.getMessageByItemCode("TASKN2"));
				notify.setDescription(message.getMessageByItemCode("TASKN1") + CMDConstrant.SPACE + creator.getName());
				notify.setType("task");
				notify.setDetailId(task.getId());
			}
			
			DateTimeFormatter dtf = null;
			LocalDate now = LocalDate.now();
			dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate dt = LocalDate.parse(finishDate, dtf);
			if(now.isAfter(dt)) {
				status = statusRepositotyImpl.findByIndexAndType(6, "task");
			}else {
//				status = statusRepositotyImpl.findById(statusId);
				status = statusRepositotyImpl.findByIndexAndType(1,"task");
				if (status == null) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("NOT FOUND", message.getMessageByItemCode("STAE1"), ""));
				}
			}

			task.setId(id);
			task.setCreator(creator);
			task.setReceiver(receiver);
			task.setStatus(status);
			task.setTitle(title);
			task.setDescription(description);
			task.setCreateDate(createDate);
			task.setFinishDate(finishDate);
			task.setModifyDate(modifyDate);
			task.setRate(rate);
			task.setPriority(priority);
			task.setStartDate(startDate);
			taskModel = taskRepository.edit(task, CMDConstrant.UPDATE_TASK, !CMDConstrant.CHANGE_STATUS_TASK,
					!CMDConstrant.REOPEN_TASK, null);
			if (null != taskModel) {
				notifyRepositoryImpl.add(notify);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode(messageCode), taskModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Error", message.getMessageByItemCode("TASKE3"), taskModel));

			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in edit()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	// filter
	public ResponseEntity<Object> filter(String createFrom, String createTo, String finishFrom, String finishTo,
			String title, String creator, String receiver, String dep, String order, String page, String sort) {
		createFrom = createFrom == null ? " " : createFrom.trim();
		createTo = createTo == null ? " " : createTo.trim();
		finishFrom = finishFrom == null ? " " : finishFrom.trim();
		finishTo = finishTo == null ? " " : finishTo.trim();
		dep = dep == null ? " " : dep.trim();
		title = title == null ? " " : title.trim();
		creator = creator == null ? " " : creator.trim();
		receiver = receiver == null ? " " : receiver.trim();
		order = order == null ? "DESC" : order;
		sort = sort == null ? "id" : sort;
		page = page == null ? "1" : page.trim();
		List<TaskModel> tasks = new ArrayList<TaskModel>();
		try {
			int limit = 15;
			tasks = taskRepository.filter(createFrom, createTo, finishFrom, finishTo, title, creator, receiver, dep,
					limit, order, page, sort);
			Map<String, Object> results = new TreeMap<String, Object>();
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(taskRepository.countFilter(createFrom, createTo, finishFrom, finishTo, title,
					creator, receiver, dep));
			results.put("pagination", pagination);
			results.put("tasks", tasks);
			if (results.size() > 0) {
				// Count by status
				List<TaskModel> taskForCount = taskRepository.filter("", "", "", "", "", "", "", "", -1, order, "",
						sort);
				List<StatusModel> statusModels = new ArrayList<>();
				List<Status> statuses = statusRepositotyImpl.findAllForTask();
				for (Status status : statuses) {
					int count = 0;
					for (TaskModel tModel : taskForCount) {
						if (tModel.getStatus().getId() == status.getId()) {
							count++;
						}
					}
					StatusModel statusModel = new StatusModel();
					statusModel.setId(status.getId());
					statusModel.setName(status.getName());
					statusModel.setCountByStatus(count);
					statusModels.add(statusModel);
				}
				results.put("countByStatuses", statusModels);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully ", results));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Not found ", "Can not find task list ", tasks));
			}
		} catch (Exception e) {
			LOGGER.error("ERROR: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject("ERROR ", e.getMessage(), ""));
		}

	}

	public ResponseEntity<Object> findAllHistoryByTaskID(Integer taskId) {
		List<TaskHis> taskHis = new ArrayList<TaskHis>();
		taskHis = taskRepository.findAllHistoryByTaskID(taskId);
		if (taskHis == null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("Not found ", "Can not find task list ", taskHis));
		} else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "Query produce successfully ", taskHis));
		}
	}

	public ResponseEntity<Object> findAllTaskAssigeToMe(String json, String sort, String order, String page) {

		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String startDate = null;
		String finishDate = null;
		List<Integer> creatorIds = new ArrayList<Integer>();
		List<Integer> statusIds = new ArrayList<Integer>();
		List<Integer> departmentIds = new ArrayList<Integer>();
		;
		List<TaskModel> taskModelResult = null;
		List<TaskModel> taskModelListTMP = null;
		Integer rate = null;
		Integer limit = CMDConstrant.LIMIT;
		Integer totalItem = 0;
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		try {
			page = page == null ? "1" : page.trim();
			int offset = (Integer.valueOf(page) - 1) * limit;
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonCreatorIds = jsonObject.get("creatorIds");
//				JsonNode jsonReceiverIds = jsonObject.get("receiverIds");
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			rate = (jsonObject.get("rate") != null && !jsonObject.get("rate").asText().equals("null")
					&& jsonObject.get("rate").asInt() != 0) ? jsonObject.get("rate").asInt() : null;

			startDate = (jsonObject.get("startDate") != null && !jsonObject.get("startDate").asText().equals("null")
					&& !jsonObject.get("startDate").asText().equals("")) ? jsonObject.get("startDate").asText() : null;
			finishDate = (jsonObject.get("finishDate") != null && !jsonObject.get("finishDate").asText().equals("null")
					&& !jsonObject.get("finishDate").asText().equals("")) ? jsonObject.get("finishDate").asText()
							: null;
			for (JsonNode creatorId : jsonCreatorIds) {
				creatorIds.add(Integer.valueOf(creatorId.toString()));
			}
			for (JsonNode statusId : jsonStatusObject) {
				statusIds.add(Integer.valueOf(statusId.toString()));
			}
			for (JsonNode jsonNode : jsonObject.get("departmentIds")) {
				departmentIds.add(jsonNode.asInt());
			}
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "id";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			if (statusIds.size() == 0) {
				List<Status> statuses = statusRepositotyImpl.findAllForTask();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
			}

			taskModelListTMP = taskRepository.findAllTaskAssigeToMe(userDetail.getId(), creatorIds, departmentIds,
					statusIds, rate, startDate, finishDate, sort, order, offset, limit);
			totalItem = taskRepository.countAllTaskAssigeToMe(userDetail.getId(), creatorIds, departmentIds, statusIds,
					rate, startDate, finishDate, sort, order, totalItem, limit);
			taskModelResult = new ArrayList<TaskModel>();
			if (departmentIds.size() > 0) {
				for (int i = offset; i < totalItem; i++) {
					if (taskModelResult.size() >= limit) {
						break;
					}
					taskModelResult.add(taskModelListTMP.get(i));
				}
			} else {
				taskModelResult = taskModelListTMP;
			}
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(totalItem);
			Map<String, Object> results = new TreeMap<String, Object>();
			results.put("pagination", pagination);
			results.put("tasks", taskModelResult);

			if (results.size() > 0) {
				// Count by status
				List<Status> statuses = statusRepositotyImpl.findAllForTask();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
				List<TaskModel> taskForCount = taskRepository.findAllTaskAssigeToMe(userDetail.getId(),
						new ArrayList<Integer>(), new ArrayList<Integer>(), statusIds, null, null, null, sort, order,
						-1, -1);
				List<StatusModel> statusModels = new ArrayList<>();
				for (Status status : statuses) {
					int count = 0;
					for (TaskModel tModel : taskForCount) {
						if (tModel.getStatus().getId() == status.getId()) {
							count++;
						}
					}
					StatusModel statusModel = new StatusModel();
					statusModel.setId(status.getId());
					statusModel.setName(status.getName());
					statusModel.setCountByStatus(count);
					statusModels.add(statusModel);
				}
				results.put("countByStatuses", statusModels);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}

	}

	public ResponseEntity<Object> findAllTaskTaskCreatedByMe(String json, String sort, String order, String page) {

		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String startDate = null;
		String finishDate = null;
		List<Integer> receiverIds = new ArrayList<Integer>();
		List<Integer> statusIds = new ArrayList<Integer>();
		List<Integer> departmentIds = new ArrayList<Integer>();
		;
		List<TaskModel> taskModelResult = null;
		List<TaskModel> taskModelListTMP = null;
		Integer rate = null;
		Integer limit = CMDConstrant.LIMIT;
		Integer totalItem = 0;
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		try {
			page = page == null ? "1" : page.trim();
			int offset = (Integer.valueOf(page) - 1) * limit;
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonCreatorIds = jsonObject.get("receiverIds");
//				JsonNode jsonReceiverIds = jsonObject.get("receiverIds");
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			rate = (jsonObject.get("rate") != null && !jsonObject.get("rate").asText().equals("null")
					&& jsonObject.get("rate").asInt() != 0) ? jsonObject.get("rate").asInt() : null;

			startDate = (jsonObject.get("startDate") != null && !jsonObject.get("startDate").asText().equals("null")
					&& !jsonObject.get("startDate").asText().equals("")) ? jsonObject.get("startDate").asText() : null;
			finishDate = (jsonObject.get("finishDate") != null && !jsonObject.get("finishDate").asText().equals("null")
					&& !jsonObject.get("finishDate").asText().equals("")) ? jsonObject.get("finishDate").asText()
							: null;
			for (JsonNode receiverId : jsonCreatorIds) {
				receiverIds.add(Integer.valueOf(receiverId.toString()));
			}
			for (JsonNode statusId : jsonStatusObject) {
				statusIds.add(Integer.valueOf(statusId.toString()));
			}
			for (JsonNode jsonNode : jsonObject.get("departmentIds")) {
				departmentIds.add(jsonNode.asInt());
			}
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "id";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			if (statusIds.size() == 0) {
				List<Status> statuses = statusRepositotyImpl.findAllForTask();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
			}

			taskModelListTMP = taskRepository.findAllTaskCreatedByMe(userDetail.getId(), receiverIds, departmentIds,
					statusIds, rate, startDate, finishDate, sort, order, offset, limit);
			totalItem = taskRepository.countAllTaskCreatedByMe(userDetail.getId(), receiverIds, departmentIds,
					statusIds, rate, startDate, finishDate, sort, order, totalItem, limit);
			taskModelResult = new ArrayList<TaskModel>();
			if (departmentIds.size() > 0) {
				for (int i = offset; i < totalItem; i++) {
					if (taskModelResult.size() >= limit) {
						break;
					}
					taskModelResult.add(taskModelListTMP.get(i));
				}
			} else {
				taskModelResult = taskModelListTMP;
			}
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(totalItem);
			Map<String, Object> results = new TreeMap<String, Object>();
			results.put("pagination", pagination);
			results.put("tasks", taskModelResult);

			if (results.size() > 0) {
				// Count by status
				List<Status> statuses = statusRepositotyImpl.findAllForTask();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
				List<TaskModel> taskForCount = taskRepository.findAllTaskCreatedByMe(userDetail.getId(),
						new ArrayList<Integer>(), new ArrayList<Integer>(), statusIds, null, null, null, sort, order,
						-1, -1);
				List<StatusModel> statusModels = new ArrayList<>();
				for (Status status : statuses) {
					int count = 0;
					for (TaskModel tModel : taskForCount) {
						if (tModel.getStatus().getId() == status.getId()) {
							count++;
						}
					}
					StatusModel statusModel = new StatusModel();
					statusModel.setId(status.getId());
					statusModel.setName(status.getName());
					statusModel.setCountByStatus(count);
					statusModels.add(statusModel);
				}
				results.put("countByStatuses", statusModels);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}

	}

	public ResponseEntity<Object> changeStatus(String id) {
		try {
			TaskModel taskModel = null;
			Task task = taskRepository.findByIdToEdit(Integer.valueOf(id));
			if (task.getStatus().getId() == CMDConstrant.DONE_STATUS
					|| task.getStatus().getId() == CMDConstrant.CANCEL_STATUS) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TASKE5"), ""));
			}
			Status status = null;
			if (task.getCreator().getId() != task.getReceiver().getId()) {
				status = statusRepositotyImpl.findByIndexAndType(task.getStatus().getIndex() + 1, "task");
			} else if (task.getCreator().getId() == task.getReceiver().getId()
					&& task.getStatus().getIndex() == CMDConstrant.INPROGESS_STATUS) {
				status = statusRepositotyImpl.findByIndexAndType(task.getStatus().getIndex() + 2, "task");
			} else {
				status = statusRepositotyImpl.findByIndexAndType(task.getStatus().getIndex() + 1, "task");
			}

			if (status == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("NOT FOUND", message.getMessageByItemCode("STAE1"), ""));
			}
			Notify notify = null;
			if (status.getIndex() == CMDConstrant.REVIEW_STATUS) {
				notify = new Notify();
				notify.setIsRead(false);
				notify.setReceiver(task.getCreator());
				notify.setTitle(message.getMessageByItemCode("TASKN5"));
				notify.setDescription(
						message.getMessageByItemCode("TASKN6") + CMDConstrant.SPACE + task.getReceiver().getName());
				notify.setType("task");
				notify.setDetailId(task.getId());
			} else if (status.getIndex() == CMDConstrant.DONE_STATUS) {
				notify = new Notify();
				notify.setIsRead(false);
				notify.setReceiver(task.getReceiver());
				notify.setTitle(message.getMessageByItemCode("TASKN7"));
				notify.setDescription(
						task.getCreator().getName() + CMDConstrant.SPACE + message.getMessageByItemCode("TASKN8"));
				notify.setType("task");
				notify.setDetailId(task.getId());
			}
			task.setStatus(status);
			taskModel = taskRepository.edit(task, !CMDConstrant.UPDATE_TASK, CMDConstrant.CHANGE_STATUS_TASK,
					!CMDConstrant.REOPEN_TASK, null);

			if (null != taskModel) {
				if (notify != null) {
					notifyRepositoryImpl.add(notify);
				}
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("TASKS5"), taskModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Error", message.getMessageByItemCode("TASKE3"), taskModel));

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> reopen(String json) {
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		TaskModel taskModel = null;
		try {
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			jsonObject = jsonMapper.readTree(json);
			Integer id = jsonObject.get("id") != null ? jsonObject.get("id").asInt() : -1;
			if (id == CMDConstrant.FAILED) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TASKE6"), ""));
			}

			String reason = jsonObject.get("reason") != null ? jsonObject.get("reason").asText() : "";

			Task task = taskRepository.findByIdToEdit(Integer.valueOf(id));

			if (task.getStatus().getId() == CMDConstrant.DONE_STATUS
					|| task.getStatus().getId() == CMDConstrant.CANCEL_STATUS) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TASKE5"), ""));
			}
			Status status = statusRepositotyImpl.findById(CMDConstrant.NEW_STATUS);
			if (status == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("NOT FOUND", message.getMessageByItemCode("STAE1"), ""));
			}
			task.setStatus(status);
			taskModel = taskRepository.edit(task, !CMDConstrant.UPDATE_TASK, !CMDConstrant.CHANGE_STATUS_TASK,
					CMDConstrant.REOPEN_TASK, reason);

			if (null != taskModel) {
				Notify notify = new Notify();
				if (userDetail.getId() == task.getCreator().getId()) {
					notify.setIsRead(false);
					notify.setReceiver(task.getReceiver());
					notify.setTitle(message.getMessageByItemCode("TASKN4"));
					notify.setDescription(
							task.getCreator().getName() + CMDConstrant.SPACE + message.getMessageByItemCode("TASKN3"));
					notify.setType("task");
					notify.setDetailId(task.getId());
				} else {
					notify.setIsRead(false);
					notify.setReceiver(task.getCreator());
					notify.setTitle(message.getMessageByItemCode("TASKN4"));
					notify.setDescription(
							task.getReceiver().getName() + CMDConstrant.SPACE + message.getMessageByItemCode("TASKN3"));
					notify.setType("task");
					notify.setDetailId(task.getId());
				}

				notifyRepositoryImpl.add(notify);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("TASKS5"), taskModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Error", message.getMessageByItemCode("TASKE3"), taskModel));

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}



}
