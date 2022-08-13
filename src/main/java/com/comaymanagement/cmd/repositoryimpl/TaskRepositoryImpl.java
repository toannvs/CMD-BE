package com.comaymanagement.cmd.repositoryimpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.constant.CMDConstrant;
import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Status;
import com.comaymanagement.cmd.entity.Task;
import com.comaymanagement.cmd.entity.TaskHis;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.model.EmployeeModel;
import com.comaymanagement.cmd.model.StatusModel;
import com.comaymanagement.cmd.model.TaskHisModel;
import com.comaymanagement.cmd.model.TaskHisModelTemp;
import com.comaymanagement.cmd.model.TaskModel;
import com.comaymanagement.cmd.repository.ITaskRepository;
import com.comaymanagement.cmd.service.UserDetailsImpl;

@Repository
@Transactional(rollbackFor = Exception.class)
public class TaskRepositoryImpl implements ITaskRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepositoryImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	TaskHistoryRepositoryImpl taskHistoryRepositoryImpl;
	
	@Autowired
	EmployeeRepositoryImpl employeeRepositoryImpl;
	
	@Autowired
	StatusRepositotyImpl statusRepositotyImpl;
	@Autowired
	Message message;
	
	@Override
	public List<TaskModel> findByStatusId(String statusId, String sort, String order, Integer offset, Integer limit) {
		List<Task> taskList = new ArrayList<Task>();
		List<TaskModel> customTaskList = new ArrayList<TaskModel>();
		StringBuilder hql = new StringBuilder("FROM tasks AS t ");
		hql.append("WHERE t.status.id = :statusId");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("statusId", statusId);

			query.setFirstResult(offset);
			query.setMaxResults(limit);

			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Task task = (Task) obj;
				taskList.add(task);
			}
			for (Task task : taskList) {
				PriorityQueue<Department> departmentList = new PriorityQueue<>(new TaskComparator());
				TaskModel customTask = new TaskModel();
				for (Department d : task.getCreator().getDepartments()) {
					departmentList.add(d);
				}
				customTask.setId(task.getId());
				customTask.setTitle(task.getTitle());
				EmployeeModel creatorTemp = new EmployeeModel();
				creatorTemp.setId(task.getCreator().getId());
				creatorTemp.setCode(task.getCreator().getCode());
				creatorTemp.setName(task.getCreator().getName());
				creatorTemp.setAvatar(task.getCreator().getAvatar());
				creatorTemp.setGender(task.getCreator().getGender());
				creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
				creatorTemp.setEmail(task.getCreator().getEmail());
				creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
				creatorTemp.setActive(task.getCreator().isActive());
				creatorTemp.setCreateDate(task.getCreator().getCreateDate());
				customTask.setCreator(creatorTemp);

				EmployeeModel receiverTemp = new EmployeeModel();
				receiverTemp.setId(task.getReceiver().getId());
				receiverTemp.setCode(task.getReceiver().getCode());
				receiverTemp.setName(task.getReceiver().getName());
				receiverTemp.setAvatar(task.getReceiver().getAvatar());
				receiverTemp.setGender(task.getReceiver().getGender());
				receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
				receiverTemp.setEmail(task.getReceiver().getEmail());
				receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
				receiverTemp.setActive(task.getReceiver().isActive());
				receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
				customTask.setFinishDate(task.getFinishDate());
				customTask.setStartDate(task.getStartDate());
				customTask.setReceiver(receiverTemp);

				customTask.setCreateDate(task.getCreateDate());
				customTask.setModifyDate(task.getModifyDate());
				List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
				for (Department department : task.getCreator().getDepartments()) {
					DepartmentModel dModel = new DepartmentModel();
					dModel.setCode(department.getCode());
					dModel.setDescription(department.getDescription());
					dModel.setId(department.getId());
					dModel.setName(department.getName());
					dModel.setLevel(department.getLevel());
					departmentModels.add(dModel);
				}
				customTask.setDepartment(departmentModels);
				customTask.setStatus(task.getStatus());
				customTask.setRate(task.getRate());
				customTask.setPriority(task.getPriority());
				customTaskList.add(customTask);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in findByStatusId() ", e);
		}

		return customTaskList;
	}

	@Override
	public List<TaskModel> findAll(List<Integer> departmentIds, String title, List<Integer> statusIds, List<Integer> creatorIds, List<Integer> receiverIds,
			String startDate, String finishDate, String priority, String rate, String sort, String order,
			Integer offset, Integer limit) {
		Set<Task> taskSet = new LinkedHashSet<Task>();
		List<TaskModel> customTaskList = new ArrayList<TaskModel>();
		StringBuilder hql = new StringBuilder("FROM tasks AS t ");
		hql.append("INNER JOIN t.creator as c ");
		hql.append("INNER JOIN t.status as s ");
		hql.append("INNER JOIN t.receiver as r ");
		hql.append("WHERE t.title LIKE CONCAT('%',:title,'%') ");
		if(statusIds.size()>0) {
			hql.append("AND s.id IN :statusIds ");
		}
		if(creatorIds.size()>0) {
			hql.append("AND c.id IN :creatorIds ");
		}
		if(receiverIds.size()>0) {
			hql.append("AND r.id IN :receiverIds ");
		}

		hql.append("AND t.createDate LIKE CONCAT('%',:startDate,'%') ");
		hql.append("AND t.finishDate LIKE CONCAT('%',:finishDate,'%') ");
		hql.append("AND t.priority LIKE CONCAT('%',:priority,'%') ");
		hql.append("AND t.rate LIKE CONCAT('%',:rate,'%') ");
		hql.append("order by t." + sort + " " + order);

		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("title", title);
			if(statusIds.size()>0) {
				query.setParameter("statusIds", statusIds);
			}
			if(creatorIds.size()>0) {
				query.setParameter("creatorIds", creatorIds);
			}
			if(receiverIds.size()>0) {
				query.setParameter("receiverIds", receiverIds);
			}
			query.setParameter("startDate", startDate);
			query.setParameter("finishDate", finishDate);
			query.setParameter("priority", priority);
			query.setParameter("rate", rate);
			if (departmentIds.size()==0 && offset != -1 && limit !=-1) {
				query.setFirstResult(offset);
				query.setMaxResults(limit);
			}

			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Task task = (Task) obj[0];
				taskSet.add(task);
			}
			for (Task task : taskSet) {
				TaskModel customTask = new TaskModel();
				customTask.setId(task.getId());
				customTask.setTitle(task.getTitle());

				EmployeeModel creatorTemp = new EmployeeModel();
				creatorTemp.setId(task.getCreator().getId());
				creatorTemp.setCode(task.getCreator().getCode());
				creatorTemp.setName(task.getCreator().getName());
				creatorTemp.setAvatar(task.getCreator().getAvatar());
				creatorTemp.setGender(task.getCreator().getGender());
				creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
				creatorTemp.setEmail(task.getCreator().getEmail());
				creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
				creatorTemp.setActive(task.getCreator().isActive());
				creatorTemp.setCreateDate(task.getCreator().getCreateDate());
				customTask.setCreator(creatorTemp);

				EmployeeModel receiverTemp = new EmployeeModel();
				receiverTemp.setId(task.getReceiver().getId());
				receiverTemp.setCode(task.getReceiver().getCode());
				receiverTemp.setName(task.getReceiver().getName());
				receiverTemp.setAvatar(task.getReceiver().getAvatar());
				receiverTemp.setGender(task.getReceiver().getGender());
				receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
				receiverTemp.setEmail(task.getReceiver().getEmail());
				receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
				receiverTemp.setActive(task.getReceiver().isActive());
				receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
				customTask.setReceiver(receiverTemp);

				customTask.setCreateDate(task.getCreateDate());
				customTask.setFinishDate(task.getFinishDate());
				customTask.setModifyDate(task.getModifyDate());
				customTask.setStartDate(task.getStartDate());

				List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
				for (Department department : task.getCreator().getDepartments()) {
					DepartmentModel dModel = new DepartmentModel();
					dModel.setCode(department.getCode());
					dModel.setDescription(department.getDescription());
					dModel.setId(department.getId());
					dModel.setName(department.getName());
					dModel.setLevel(department.getLevel());
					departmentModels.add(dModel);
				}
				List<TaskHis> taskHis = taskHistoryRepositoryImpl.findById(customTask.getId());
				Collections.sort(taskHis, new Comparator<TaskHis>() {
					@Override
					public int compare(TaskHis o1, TaskHis o2) {
						return o2.getId().compareTo(o1.getId());
					}
				});

				List<TaskHisModelTemp> taskHisModelTemps = new ArrayList<TaskHisModelTemp>();
				for (TaskHis itemTaskHis : taskHis) {
					TaskHisModelTemp taskHisModel = new TaskHisModelTemp();
					taskHisModel.setId(itemTaskHis.getId());
					taskHisModel.setModifyDate(itemTaskHis.getModifyDate());
					taskHisModel.setStatus(itemTaskHis.getStatus());
					taskHisModel.setMessage(itemTaskHis.getMessage());
					
					EmployeeModel editor = new EmployeeModel();
					editor.setId(itemTaskHis.getModifyBy().getId());
					editor.setCode(itemTaskHis.getModifyBy().getCode());
					editor.setName(itemTaskHis.getModifyBy().getName());
					editor.setAvatar(itemTaskHis.getModifyBy().getAvatar());
					editor.setGender(itemTaskHis.getModifyBy().getGender());
					editor.setDateOfBirth(itemTaskHis.getModifyBy().getDateOfBirth());
					editor.setEmail(itemTaskHis.getModifyBy().getEmail());
					editor.setPhoneNumber(itemTaskHis.getModifyBy().getPhoneNumber());
					editor.setActive(itemTaskHis.getModifyBy().isActive());
					editor.setCreateDate(itemTaskHis.getModifyBy().getCreateDate());
					taskHisModel.setModifyBy(editor);
					
					taskHisModel.setTaskId(itemTaskHis.getTaskId());

					taskHisModelTemps.add(taskHisModel);
				}

				List<TaskHisModel> taskHisModels = null;
				taskHisModels = new ArrayList<TaskHisModel>();
				for (TaskHisModelTemp itemHisModelTemp : taskHisModelTemps) {
					TaskHisModel taskHistoryModel = new TaskHisModel();
					taskHistoryModel.setMessage(itemHisModelTemp.getMessage());
					taskHistoryModel.setModifyBy(itemHisModelTemp.getModifyBy().getName());
					taskHistoryModel.setModifyDate(itemHisModelTemp.getModifyDate());
					taskHistoryModel.setModifyById(itemHisModelTemp.getModifyBy().getId());
					taskHistoryModel.setAvatar(itemHisModelTemp.getModifyBy().getAvatar());
					taskHisModels.add(taskHistoryModel);
				}
				
				customTask.setDepartment(departmentModels);
				customTask.setStatus(task.getStatus());
				customTask.setDescription(task.getDescription());
				customTask.setRate(task.getRate());
				customTask.setPriority(task.getPriority());
				customTask.setTaskHis(taskHisModels);
				
				if (departmentIds.size()==0) {
					customTaskList.add(customTask);
				} else {
					boolean addFlag = false;
					for (DepartmentModel departmentModel : departmentModels) {
						for(Integer id : departmentIds) {
							if (departmentModel.getId() == id) {
								addFlag = true;
							}
						}

					}
					if (addFlag) {
						customTaskList.add(customTask);
					}
				}
				

			}

		} catch (Exception e) {
			LOGGER.error("Error has occured in findAll() ", e);
		}
		return customTaskList;
	}

	@Override
	public Integer countAllPaging(List<Integer> departmentIds, String title, List<Integer> statusIds, List<Integer> creatorIds, List<Integer> receiverIds,
			String startDate, String finishDate, String priority, String rate, String sort, String order,
			Integer offset, Integer limit) {
		Integer count = 0;
		StringBuilder hql = new StringBuilder("FROM tasks AS t ");
		hql.append("INNER JOIN t.creator as c ");
		hql.append("INNER JOIN t.status as s ");
		hql.append("INNER JOIN t.receiver as r ");
//		hql.append("INNER JOIN c.departments as dep ");
//		hql.append("WHERE dep.name LIKE CONCAT('%',:dep,'%') ");
		hql.append("WHERE t.title LIKE CONCAT('%',:title,'%') ");
		if(statusIds.size()>0) {
			hql.append("AND s.id IN (:statusIds) ");
		}
		if(creatorIds.size()>0) {
			hql.append("AND c.id IN (:creatorIds) ");
		}
		if(receiverIds.size()>0) {
			hql.append("AND r.id IN (:receiverIds) ");
		}

		hql.append("AND t.createDate LIKE CONCAT('%',:startDate,'%') ");
		hql.append("AND t.finishDate LIKE CONCAT('%',:finishDate,'%') ");
		hql.append("AND t.priority LIKE CONCAT('%',:priority,'%') ");
		hql.append("AND t.rate LIKE CONCAT('%',:rate,'%') ");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("title", title);
			if(statusIds.size()>0) {
				query.setParameter("statusIds", statusIds);
			}
			if(creatorIds.size()>0) {
				query.setParameter("creatorIds", creatorIds);
			}
			if(receiverIds.size()>0) {
				query.setParameter("receiverIds", receiverIds);
			}
			query.setParameter("startDate", startDate);
			query.setParameter("finishDate", finishDate);
			query.setParameter("priority", priority);
			query.setParameter("rate", rate);
//			if (departmentIds.size()==0) {
//				query.setFirstResult(offset);
//				query.setMaxResults(limit);
//			}

			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Task task = (Task) obj[0];
				if (departmentIds.size()==0) {
					count++;
				} else {
					boolean addFlag = false;
					for (Department department : task.getCreator().getDepartments()) {
						for(Integer id : departmentIds) {
							if (department.getId() == id) {
								addFlag = true;
							}
						}
					}
					if (addFlag) {
						count++;
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in countAll() ", e);
		}
		return count;
	}

	public Integer countAllPagingDuplicate(String dep, String title, String status, String creator, String receiver,
			String createDate, String finishDate, String priority, String rate, String sort, String order) {
		Integer count = 0;
		StringBuilder hql = new StringBuilder("FROM tasks AS t ");
		hql.append("INNER JOIN t.creator as c ");
		hql.append("INNER JOIN t.status as s ");
		hql.append("INNER JOIN t.receiver as r ");
		hql.append("INNER JOIN c.departments as dep ");
		hql.append("WHERE dep.name LIKE CONCAT('%',:dep,'%') ");
		hql.append("AND t.title LIKE CONCAT('%',:title,'%') ");
		hql.append("AND s.name LIKE CONCAT('%',:status,'%') ");
		hql.append("AND c.name LIKE CONCAT('%',:creator,'%') ");
		hql.append("AND r.name LIKE CONCAT('%',:receiver,'%') ");
		hql.append("AND t.priority LIKE CONCAT('%',:priority,'%') ");
		hql.append("AND t.rate LIKE CONCAT('%',:rate,'%') ");
		hql.append("AND t.createDate LIKE CONCAT('%',:createDate,'%') ");
		hql.append("AND t.finishDate LIKE CONCAT('%',:finishDate,'%') ");
		hql.append("order by t." + sort + " " + order);

		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("dep", dep);
			query.setParameter("title", title);
			query.setParameter("status", status);
			query.setParameter("creator", creator);
			query.setParameter("receiver", receiver);
			query.setParameter("createDate", createDate);
			query.setParameter("finishDate", finishDate);
			query.setParameter("priority", priority);
			query.setParameter("rate", rate);

			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				it.next();
				count++;
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in countAll() ", e);
		}
		return count;
	}

	@Override
	public List<TaskModel> findByStatusIds(List<Integer> statusIds, String sort, String order, Integer offset,
			Integer limit) {
		List<Task> tasks = new ArrayList<Task>();
		List<TaskModel> customTasks = new ArrayList<TaskModel>();
		StringBuilder hql = new StringBuilder("FROM tasks AS t ");
		hql.append("WHERE status_id IN (:ids) ");
		hql.append("ORDER BY " + sort + " " + order);
		try {
			LOGGER.info(hql.toString());
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("ids", statusIds);
			query.setFirstResult(offset);
			query.setMaxResults(limit);
			tasks = query.getResultList();
			for (Task task : tasks) {
				PriorityQueue<Department> departmentQueue = new PriorityQueue<>(new TaskComparator());
				for (Department d : task.getCreator().getDepartments()) {
					departmentQueue.add(d);
				}
				TaskModel customTask = new TaskModel();
				customTask.setId(task.getId());
				customTask.setTitle(task.getTitle());

				EmployeeModel creatorTemp = new EmployeeModel();
				creatorTemp.setId(task.getCreator().getId());
				creatorTemp.setCode(task.getCreator().getCode());
				creatorTemp.setName(task.getCreator().getName());
				creatorTemp.setAvatar(task.getCreator().getAvatar());
				creatorTemp.setGender(task.getCreator().getGender());
				creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
				creatorTemp.setEmail(task.getCreator().getEmail());
				creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
				creatorTemp.setActive(task.getCreator().isActive());
				creatorTemp.setCreateDate(task.getCreator().getCreateDate());
				customTask.setCreator(creatorTemp);

				EmployeeModel receiverTemp = new EmployeeModel();
				receiverTemp.setId(task.getReceiver().getId());
				receiverTemp.setCode(task.getReceiver().getCode());
				receiverTemp.setName(task.getReceiver().getName());
				receiverTemp.setAvatar(task.getReceiver().getAvatar());
				receiverTemp.setGender(task.getReceiver().getGender());
				receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
				receiverTemp.setEmail(task.getReceiver().getEmail());
				receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
				receiverTemp.setActive(task.getReceiver().isActive());
				customTask.setReceiver(receiverTemp);

				customTask.setCreateDate(task.getCreateDate());
				customTask.setFinishDate(task.getFinishDate());

				List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
				for (Department department : task.getCreator().getDepartments()) {
					DepartmentModel dModel = new DepartmentModel();
					dModel.setCode(department.getCode());
					dModel.setDescription(department.getDescription());
					dModel.setId(department.getId());
					dModel.setName(department.getName());
					dModel.setLevel(department.getLevel());
					departmentModels.add(dModel);
				}
				customTask.setDepartment(departmentModels);
				customTask.setStatus(task.getStatus());
				customTask.setDescription(task.getDescription());
				customTask.setRate(task.getRate());
				customTask.setPriority(task.getPriority());
				customTask.setFinishDate(task.getFinishDate());
				customTask.setStartDate(task.getStartDate());
				customTask.setModifyDate(task.getModifyDate());
				receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
				customTasks.add(customTask);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in findByStatusIds() ", e);
		}
		return customTasks;
	}

	@Override
	public Integer countFindByIds(List<Integer> ids) {
		Integer count = null;
		StringBuilder hql = new StringBuilder("SELECT COUNT(*) FROM tasks AS t ");
		hql.append("WHERE status_id IN (:ids)");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("ids", ids);
			LOGGER.info(hql.toString());
			@SuppressWarnings("rawtypes")
			List list = query.getResultList();
			count = Integer.parseInt(list.get(0).toString());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return count;
	}

	@Override
	public TaskModel add(Task task) throws Exception {
		try {
			LOGGER.info("SAVE TASK....");
			Session session = sessionFactory.getCurrentSession();
			TaskModel customTask = null;
			Integer resultInteger = Integer.parseInt(session.save(task).toString());
			if (resultInteger > CMDConstrant.FAILED) {
				customTask = new TaskModel();
				customTask.setId(task.getId());
				customTask.setTitle(task.getTitle());

				EmployeeModel creatorTemp = new EmployeeModel();
				creatorTemp.setId(task.getCreator().getId());
				creatorTemp.setCode(task.getCreator().getCode());
				creatorTemp.setName(task.getCreator().getName());
				creatorTemp.setAvatar(task.getCreator().getAvatar());
				creatorTemp.setGender(task.getCreator().getGender());
				creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
				creatorTemp.setEmail(task.getCreator().getEmail());
				creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
				creatorTemp.setActive(task.getCreator().isActive());
				creatorTemp.setCreateDate(task.getCreator().getCreateDate());
				customTask.setCreator(creatorTemp);

				EmployeeModel receiverTemp = new EmployeeModel();
				receiverTemp.setId(task.getReceiver().getId());
				receiverTemp.setCode(task.getReceiver().getCode());
				receiverTemp.setName(task.getReceiver().getName());
				receiverTemp.setAvatar(task.getReceiver().getAvatar());
				receiverTemp.setGender(task.getReceiver().getGender());
				receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
				receiverTemp.setEmail(task.getReceiver().getEmail());
				receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
				receiverTemp.setActive(task.getReceiver().isActive());
				receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
				customTask.setReceiver(receiverTemp);

				List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
				for (Department department : task.getCreator().getDepartments()) {
					DepartmentModel dModel = new DepartmentModel();
					dModel.setCode(department.getCode());
					dModel.setDescription(department.getDescription());
					dModel.setId(department.getId());
					dModel.setName(department.getName());
					dModel.setLevel(department.getLevel());
					departmentModels.add(dModel);
				}
				customTask.setDepartment(departmentModels);
				customTask.setStatus(task.getStatus());
				customTask.setDescription(task.getDescription());
				customTask.setRate(task.getRate());
				customTask.setPriority(task.getPriority());
				customTask.setCreateDate(task.getCreateDate());
				customTask.setFinishDate(task.getFinishDate());
				customTask.setStartDate(task.getStartDate());
				customTask.setModifyDate(task.getModifyDate());
				
				TaskHis taskHis = new TaskHis();
				taskHis.setTaskId(customTask.getId());
				taskHis.setReceiver(task.getReceiver());
				taskHis.setStatus(task.getStatus());
				taskHis.setModifyDate(task.getCreateDate());
				taskHis.setMessage(message.getMessageByItemCode("TASKM1"));
				
				UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
						.getPrincipal();
				Employee modifyBy = employeeRepositoryImpl.findById(userDetail.getId());
				taskHis.setModifyBy(modifyBy);
				Integer addTaskHisResult  = taskHistoryRepositoryImpl.add(taskHis);
				
				List<TaskHisModel> taskHisModels = null;
				if(addTaskHisResult != CMDConstrant.FAILED) {
					taskHisModels = new ArrayList<TaskHisModel>();
					TaskHisModel taskHistoryModel = new TaskHisModel();
					taskHistoryModel.setMessage(taskHis.getMessage());
					taskHistoryModel.setModifyBy(modifyBy.getName());
					taskHistoryModel.setModifyDate(task.getCreateDate());
					taskHisModels.add(taskHistoryModel);
				}
				
				customTask.setTaskHis(taskHisModels);
				return customTask;
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in addEmployee() ", e);
		}
		return null;
	}

	@Override
	public Integer getMaxId() {
		StringBuilder hql = new StringBuilder("SELECT t.id FROM tasks AS t ");
		hql.append("INNER JOIN t.creator ");
		hql.append("INNER JOIN t.status ");
		hql.append("INNER JOIN t.receiver ");
		hql.append("order by t.id DESC");
		try {
			LOGGER.info(hql.toString());
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setMaxResults(1);
			List tasks = query.getResultList();
			return Integer.parseInt(tasks.get(0).toString());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	@Override
	public TaskModel findById(Integer id) {
		TaskModel customTask = null;
		StringBuilder hql = new StringBuilder("FROM tasks AS ta ");
		hql.append(" inner join ta.creator as em");
		hql.append(" inner join ta.receiver as em1");
		hql.append(" inner join ta.status as st");
		hql.append(" WHERE ta.id = :id");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("id", id);
			customTask = new TaskModel();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {

				Object[] obj = (Object[]) it.next();
				Task task = (Task) obj[0];
				PriorityQueue<Department> departmentList = new PriorityQueue<>(new TaskComparator());
				for (Department d : task.getCreator().getDepartments()) {
					departmentList.add(d);
				}
				customTask.setId(task.getId());
				customTask.setTitle(task.getTitle());
				EmployeeModel creatorTemp = new EmployeeModel();
				creatorTemp.setId(task.getCreator().getId());
				creatorTemp.setCode(task.getCreator().getCode());
				creatorTemp.setName(task.getCreator().getName());
				creatorTemp.setAvatar(task.getCreator().getAvatar());
				creatorTemp.setGender(task.getCreator().getGender());
				creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
				creatorTemp.setEmail(task.getCreator().getEmail());
				creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
				creatorTemp.setActive(task.getCreator().isActive());
				creatorTemp.setCreateDate(task.getCreator().getCreateDate());
				customTask.setCreator(creatorTemp);

				EmployeeModel receiverTemp = new EmployeeModel();
				receiverTemp.setId(task.getReceiver().getId());
				receiverTemp.setCode(task.getReceiver().getCode());
				receiverTemp.setName(task.getReceiver().getName());
				receiverTemp.setAvatar(task.getReceiver().getAvatar());
				receiverTemp.setGender(task.getReceiver().getGender());
				receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
				receiverTemp.setEmail(task.getReceiver().getEmail());
				receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
				receiverTemp.setActive(task.getReceiver().isActive());
				receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
				customTask.setReceiver(receiverTemp);
				


				List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
				for (Department department : departmentList) {
					DepartmentModel dModel = new DepartmentModel();
					dModel.setCode(department.getCode());
					dModel.setDescription(department.getDescription());
					dModel.setId(department.getId());
					dModel.setName(department.getName());
					dModel.setLevel(department.getLevel());
					departmentModels.add(dModel);
				}
				customTask.setDepartment(departmentModels);
				customTask.setStatus(task.getStatus());
				customTask.setDescription(task.getDescription());
				customTask.setRate(task.getRate());
				customTask.setCreateDate(task.getCreateDate());
				customTask.setFinishDate(task.getFinishDate());
				customTask.setStartDate(task.getStartDate());
				customTask.setModifyDate(task.getModifyDate());
				customTask.setPriority(task.getPriority());
				List<TaskHis> taskHis = taskHistoryRepositoryImpl.findById(customTask.getId());
				Collections.sort(taskHis, new Comparator<TaskHis>() {
					@Override
					public int compare(TaskHis o1, TaskHis o2) {
						return o2.getId().compareTo(o1.getId());
					}
				});

				List<TaskHisModelTemp> taskHisModelTemps = new ArrayList<TaskHisModelTemp>();
				for (TaskHis itemTaskHis : taskHis) {
					TaskHisModelTemp taskHisModel = new TaskHisModelTemp();
					taskHisModel.setId(itemTaskHis.getId());
					taskHisModel.setModifyDate(itemTaskHis.getModifyDate());
					taskHisModel.setStatus(itemTaskHis.getStatus());
					taskHisModel.setMessage(itemTaskHis.getMessage());
					
//					EmployeeModel receiverHis = new EmployeeModel();
//					receiverHis.setId(itemTaskHis.getReceiver().getId());
//					receiverHis.setCode(itemTaskHis.getReceiver().getCode());
//					receiverHis.setName(itemTaskHis.getReceiver().getName());
//					receiverHis.setAvatar(itemTaskHis.getReceiver().getAvatar());
//					receiverHis.setGender(itemTaskHis.getReceiver().getGender());
//					receiverHis.setDateOfBirth(itemTaskHis.getReceiver().getDateOfBirth());
//					receiverHis.setEmail(itemTaskHis.getReceiver().getEmail());
//					receiverHis.setPhoneNumber(itemTaskHis.getReceiver().getPhoneNumber());
//					receiverHis.setActive(itemTaskHis.getReceiver().isActive());
//					receiverHis.setCreateDate(itemTaskHis.getReceiver().getCreateDate());
//					taskHisModel.setReceiver(receiverHis);

					EmployeeModel editor = new EmployeeModel();
					editor.setId(itemTaskHis.getModifyBy().getId());
					editor.setCode(itemTaskHis.getModifyBy().getCode());
					editor.setName(itemTaskHis.getModifyBy().getName());
					editor.setAvatar(itemTaskHis.getModifyBy().getAvatar());
					editor.setGender(itemTaskHis.getModifyBy().getGender());
					editor.setDateOfBirth(itemTaskHis.getModifyBy().getDateOfBirth());
					editor.setEmail(itemTaskHis.getModifyBy().getEmail());
					editor.setPhoneNumber(itemTaskHis.getModifyBy().getPhoneNumber());
					editor.setActive(itemTaskHis.getModifyBy().isActive());
					editor.setCreateDate(itemTaskHis.getModifyBy().getCreateDate());
					taskHisModel.setModifyBy(editor);
					
					taskHisModel.setTaskId(itemTaskHis.getTaskId());

					taskHisModelTemps.add(taskHisModel);
				}

				List<TaskHisModel> taskHisModels = null;
				taskHisModels = new ArrayList<TaskHisModel>();
				for (TaskHisModelTemp itemHisModelTemp : taskHisModelTemps) {
					TaskHisModel taskHistoryModel = new TaskHisModel();
					taskHistoryModel.setMessage(itemHisModelTemp.getMessage());
					taskHistoryModel.setModifyBy(itemHisModelTemp.getModifyBy().getName());
					taskHistoryModel.setModifyDate(itemHisModelTemp.getModifyDate());
					taskHistoryModel.setModifyById(itemHisModelTemp.getModifyBy().getId());
					taskHistoryModel.setAvatar(itemHisModelTemp.getModifyBy().getAvatar());
					taskHisModels.add(taskHistoryModel);
				}

				customTask.setTaskHis(taskHisModels);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return customTask;
	}

	// delete
	public String deleteTaskById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Task task = new Task();
			task = session.find(Task.class, id);

			TaskHis th = new TaskHis();
			th = session.find(TaskHis.class, id);
			if (th != null) {
				session.remove(th);
			}
			if (task != null) {
				session.remove(task);
			}
			return "1";
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return "0";
		}
	}

	// edit
	@Override
	public TaskModel edit(Task task,boolean updateTask, boolean changeStatus, boolean reopen,String reason) throws Exception {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.update(task);
			TaskModel customTask = new TaskModel();
			customTask.setId(task.getId());
			customTask.setTitle(task.getTitle());

			EmployeeModel creatorTemp = new EmployeeModel();
			creatorTemp.setId(task.getCreator().getId());
			creatorTemp.setCode(task.getCreator().getCode());
			creatorTemp.setName(task.getCreator().getName());
			creatorTemp.setAvatar(task.getCreator().getAvatar());
			creatorTemp.setGender(task.getCreator().getGender());
			creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
			creatorTemp.setEmail(task.getCreator().getEmail());
			creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
			creatorTemp.setActive(task.getCreator().isActive());
			creatorTemp.setCreateDate(task.getCreator().getCreateDate());
			customTask.setCreator(creatorTemp);

			EmployeeModel receiverTemp = new EmployeeModel();
			receiverTemp.setId(task.getReceiver().getId());
			receiverTemp.setCode(task.getReceiver().getCode());
			receiverTemp.setName(task.getReceiver().getName());
			receiverTemp.setAvatar(task.getReceiver().getAvatar());
			receiverTemp.setGender(task.getReceiver().getGender());
			receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
			receiverTemp.setEmail(task.getReceiver().getEmail());
			receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
			receiverTemp.setActive(task.getReceiver().isActive());
			receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
			customTask.setReceiver(receiverTemp);

			List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
			for (Department department : task.getCreator().getDepartments()) {
				DepartmentModel dModel = new DepartmentModel();
				dModel.setCode(department.getCode());
				dModel.setDescription(department.getDescription());
				dModel.setId(department.getId());
				dModel.setName(department.getName());
				dModel.setLevel(department.getLevel());
				departmentModels.add(dModel);
			}
			customTask.setDepartment(departmentModels);
			customTask.setStatus(task.getStatus());
			customTask.setDescription(task.getDescription());
			customTask.setRate(task.getRate());
			customTask.setCreateDate(task.getCreateDate());
			customTask.setFinishDate(task.getFinishDate());
			customTask.setStartDate(task.getStartDate());
			customTask.setModifyDate(task.getModifyDate());
			customTask.setPriority(task.getPriority());
			
			TaskHis taskHis = new TaskHis();
			taskHis.setTaskId(customTask.getId());
			taskHis.setReceiver(task.getReceiver());
			taskHis.setStatus(task.getStatus());
			taskHis.setModifyDate(task.getModifyDate());
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			Employee modifyBy = employeeRepositoryImpl.findById(userDetail.getId());
			taskHis.setModifyBy(modifyBy);
			if(updateTask) {
				taskHis.setMessage(message.getMessageByItemCode("TASKM5"));
			}else if (changeStatus) {
				if(customTask.getStatus().getIndex() == CMDConstrant.INPROGESS_STATUS) {
					taskHis.setMessage(message.getMessageByItemCode("TASKM2"));
				}else if (customTask.getStatus().getIndex() == CMDConstrant.REVIEW_STATUS) {
					taskHis.setMessage(message.getMessageByItemCode("TASKM6"));
				}else {
					taskHis.setMessage(message.getMessageByItemCode("TASKM4"));
				}
			}else {
				taskHis.setMessage(message.getMessageByItemCode("TASKM3") + CMDConstrant.SPACE + reason);
			}
			Integer addTaskHisResult  = taskHistoryRepositoryImpl.add(taskHis);
			List<TaskHis> taskHistories = taskHistoryRepositoryImpl.findById(customTask.getId());
			Collections.sort(taskHistories, new Comparator<TaskHis>() {
				@Override
				public int compare(TaskHis o1, TaskHis o2) {
					return o2.getId().compareTo(o1.getId());
				}
			});

			List<TaskHisModelTemp> taskHisModelTemps = new ArrayList<TaskHisModelTemp>();
			for (TaskHis itemTaskHis : taskHistories) {
				TaskHisModelTemp taskHisModel = new TaskHisModelTemp();
				taskHisModel.setId(itemTaskHis.getId());
				taskHisModel.setModifyDate(itemTaskHis.getModifyDate());
				taskHisModel.setStatus(itemTaskHis.getStatus());
				taskHisModel.setMessage(itemTaskHis.getMessage());

//				EmployeeModel receiverHis = new EmployeeModel();
//				receiverHis.setId(itemTaskHis.getReceiver().getId());
//				receiverHis.setCode(itemTaskHis.getReceiver().getCode());
//				receiverHis.setName(itemTaskHis.getReceiver().getName());
//				receiverHis.setAvatar(itemTaskHis.getReceiver().getAvatar());
//				receiverHis.setGender(itemTaskHis.getReceiver().getGender());
//				receiverHis.setDateOfBirth(itemTaskHis.getReceiver().getDateOfBirth());
//				receiverHis.setEmail(itemTaskHis.getReceiver().getEmail());
//				receiverHis.setPhoneNumber(itemTaskHis.getReceiver().getPhoneNumber());
//				receiverHis.setActive(itemTaskHis.getReceiver().isActive());
//				receiverHis.setCreateDate(itemTaskHis.getReceiver().getCreateDate());
//				taskHisModel.setReceiver(receiverHis);

				EmployeeModel editor = new EmployeeModel();
				editor.setId(itemTaskHis.getModifyBy().getId());
				editor.setCode(itemTaskHis.getModifyBy().getCode());
				editor.setName(itemTaskHis.getModifyBy().getName());
				editor.setAvatar(itemTaskHis.getModifyBy().getAvatar());
				editor.setGender(itemTaskHis.getModifyBy().getGender());
				editor.setDateOfBirth(itemTaskHis.getModifyBy().getDateOfBirth());
				editor.setEmail(itemTaskHis.getModifyBy().getEmail());
				editor.setPhoneNumber(itemTaskHis.getModifyBy().getPhoneNumber());
				editor.setActive(itemTaskHis.getModifyBy().isActive());
				editor.setCreateDate(itemTaskHis.getModifyBy().getCreateDate());
				taskHisModel.setModifyBy(editor);


				taskHisModel.setTaskId(itemTaskHis.getTaskId());

				taskHisModelTemps.add(taskHisModel);
			}
			
			
			List<TaskHisModel> taskHisModels = null;
			if(addTaskHisResult != CMDConstrant.FAILED) {
				taskHisModels = new ArrayList<TaskHisModel>();
				for(TaskHisModelTemp itemHisModelTemp : taskHisModelTemps) {
					TaskHisModel taskHistoryModel = new TaskHisModel();
					taskHistoryModel.setMessage(itemHisModelTemp.getMessage());
					taskHistoryModel.setModifyBy(itemHisModelTemp.getModifyBy().getName());
					taskHistoryModel.setModifyDate(itemHisModelTemp.getModifyDate());
					
					taskHisModels.add(taskHistoryModel);
				}

			}
			
			customTask.setTaskHis(taskHisModels);
			return customTask;

		} catch (Exception e) {
			LOGGER.error("Error has occured in edit task ", e);
		}
		return null;
	}

	// filter
	@Override
	public List<TaskModel> filter(String createFrom, String createTo, String finishFrom, String finishTo, String title,
			String creator, String receiver, String department, Integer limit, String order, String page, String sort) {
		List<Task> tasks = new ArrayList<Task>();
		List<TaskModel> customTasks = new ArrayList<TaskModel>();
		StringBuilder hql = new StringBuilder("FROM tasks AS ta");
		hql.append(" inner join ta.creator as em");
		hql.append(" inner join em.departments as de");
		hql.append(" inner join ta.receiver as em1");
		hql.append(" where em.name LIKE CONCAT('%',:creator,'%')");
		hql.append(" AND de.name LIKE CONCAT('%',:department,'%')");// department of creator
		hql.append(" AND em1.name LIKE CONCAT('%',:receiver,'%')");
		hql.append(" AND ta.title LIKE CONCAT('%',:title,'%')");
		if (createFrom.toString().length() > 5) {
			hql.append(" and ta.createDate>=:createFrom");
		}
		if (createTo.toString().length() > 5) {
			hql.append(" and ta.createDate<=:createTo");
		}
		if (finishFrom.toString().length() > 5) {
			hql.append(" and ta.finishDate>=:finishFrom");
		}
		if (finishTo.toString().length() > 5) {
			hql.append(" and ta.finishDate<=:finishTo");
		}
		hql.append(" ORDER BY ta." + sort + " " + order);
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("creator", creator);
			query.setParameter("receiver", receiver);
			query.setParameter("department", department);
			query.setParameter("title", title);
			if (createFrom.toString().length() > 5) {
				query.setParameter("createFrom", createFrom);
			}
			if (createTo.toString().length() > 5) {
				query.setParameter("createTo", createTo);
			}
			if (finishFrom.toString().length() > 5) {
				query.setParameter("finishFrom", finishFrom);
			}
			if (finishTo.toString().length() > 5) {
				query.setParameter("finishTo", finishTo);
			}
			LOGGER.info(hql.toString());
			int offset = (Integer.valueOf(page) - 1) * limit;
			if(offset!=-1 && limit!=-1) {
				query.setFirstResult(offset);
				query.setMaxResults(limit);
			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Task task = (Task) obj[0];
				tasks.add(task);
			}
			for (Task task : tasks) {
				TaskModel customTask = new TaskModel();
				customTask.setId(task.getId());
				customTask.setTitle(task.getTitle());
				EmployeeModel creatorTemp = new EmployeeModel();
				creatorTemp.setId(task.getCreator().getId());
				creatorTemp.setCode(task.getCreator().getCode());
				creatorTemp.setName(task.getCreator().getName());
				creatorTemp.setAvatar(task.getCreator().getAvatar());
				creatorTemp.setGender(task.getCreator().getGender());
				creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
				creatorTemp.setEmail(task.getCreator().getEmail());
				creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
				creatorTemp.setActive(task.getCreator().isActive());
				creatorTemp.setCreateDate(task.getCreator().getCreateDate());
				customTask.setCreator(creatorTemp);

				EmployeeModel receiverTemp = new EmployeeModel();
				receiverTemp.setId(task.getReceiver().getId());
				receiverTemp.setCode(task.getReceiver().getCode());
				receiverTemp.setName(task.getReceiver().getName());
				receiverTemp.setAvatar(task.getReceiver().getAvatar());
				receiverTemp.setGender(task.getReceiver().getGender());
				receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
				receiverTemp.setEmail(task.getReceiver().getEmail());
				receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
				receiverTemp.setActive(task.getReceiver().isActive());
				receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
				customTask.setReceiver(receiverTemp);

				List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
				for (Department departmentTmp : task.getCreator().getDepartments()) {
					DepartmentModel dModel = new DepartmentModel();
					dModel.setCode(departmentTmp.getCode());
					dModel.setDescription(departmentTmp.getDescription());
					dModel.setId(departmentTmp.getId());
					dModel.setName(departmentTmp.getName());
					dModel.setLevel(departmentTmp.getLevel());
					departmentModels.add(dModel);
				}
				customTask.setDepartment(departmentModels);
				customTask.setStatus(task.getStatus());
				customTask.setDescription(task.getDescription());
				customTask.setRate(task.getRate());
				customTask.setPriority(task.getPriority());
				customTask.setCreateDate(task.getCreateDate());
				customTask.setFinishDate(task.getFinishDate());
				customTask.setStartDate(task.getStartDate());
				customTask.setModifyDate(task.getModifyDate());
				customTasks.add(customTask);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return customTasks;
	}

	@Override
	public Integer countFilter(String createFrom, String createTo, String finishFrom, String finishTo, String title,
			String creator, String receiver, String department) {
		StringBuilder hql = new StringBuilder("SELECT COUNT(*) FROM tasks AS ta");
		hql.append(" inner join ta.creator as em");
		hql.append(" inner join em.departments as de");
		hql.append(" inner join ta.receiver as em1");
		hql.append(" where em.name LIKE CONCAT('%',:creator,'%')");
		hql.append(" AND de.name LIKE CONCAT('%',:department,'%')");// department of creator
		hql.append(" AND em1.name LIKE CONCAT('%',:receiver,'%')");
		hql.append(" AND ta.title LIKE CONCAT('%',:title,'%')");
		if (createFrom.toString().length() > 5) {
			hql.append(" and ta.createDate>=:createFrom");
		}
		if (createTo.toString().length() > 5) {
			hql.append(" and ta.createDate<=:createTo");
		}
		if (finishFrom.toString().length() > 5) {
			hql.append(" and ta.finishDate>=:finishFrom");
		}
		if (finishTo.toString().length() > 5) {
			hql.append(" and ta.finishDate<=:finishTo");
		}
		int count = 0;
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("creator", creator);
			query.setParameter("receiver", receiver);
			query.setParameter("department", department);
			query.setParameter("title", title);
			if (createFrom.toString().length() > 5) {
				query.setParameter("createFrom", createFrom);
			}
			if (createTo.toString().length() > 5) {
				query.setParameter("createTo", createTo);
			}
			if (finishFrom.toString().length() > 5) {
				query.setParameter("finishFrom", finishFrom);
			}
			if (finishTo.toString().length() > 5) {
				query.setParameter("finishTo", finishTo);
			}
			LOGGER.info(hql.toString());
			@SuppressWarnings("rawtypes")
			List list = query.getResultList();
			count = Integer.parseInt(list.get(0).toString());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return count;
	}

	@Override
	public List<TaskHis> findAllHistoryByTaskID(Integer taskId) {
		List<TaskHis> taskHis = null;
		StringBuilder hql = new StringBuilder("FROM task_his AS th ");
		hql.append(" inner join th.task as ta");
		hql.append(" inner join th.receiver as em1");
		hql.append(" inner join th.status as st");
		hql.append(" WHERE th.task.id = :taskId");
		try {
			Session session = sessionFactory.getCurrentSession();
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			query.setParameter("taskId", taskId);
			taskHis = new ArrayList<TaskHis>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				TaskHis itemTaskHis = new TaskHis();
				Object[] ob = (Object[]) it.next();
				itemTaskHis = (TaskHis) ob[0];
				taskHis.add(itemTaskHis);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return taskHis;
	}

	@Override
	public Task findByIdToEdit(Integer id) {
		Task task = null;
		StringBuilder hql = new StringBuilder("FROM tasks AS ta ");
		hql.append(" inner join ta.creator as em");
		hql.append(" inner join ta.receiver as em1");
		hql.append(" inner join ta.status as st");
		hql.append(" WHERE ta.id = :id");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("id", id);
			task = new Task();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				task = (Task) obj[0];
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return task;
	}

	@Override
	public List<TaskModel> findAllTaskAssigeToMe(Integer employeeId, List<Integer> creatorIds,
			List<Integer> departmentIds, List<Integer> statusIds, Integer rate, String startDate, String finishDate,
			String sort, String order, Integer offset, Integer limit) {
		try {
			List<TaskModel> customTasks = null;
			Session session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder();
			hql.append("FROM tasks AS tas ");
			hql.append("INNER JOIN tas.creator AS cre ");
			hql.append("INNER JOIN tas.status AS st ");
			hql.append("INNER JOIN tas.receiver ");
			hql.append("WHERE tas.receiver.id = " + employeeId);
			hql.append(" AND st.id IN (:statusIds) ");
			if(creatorIds.size()>0) {
				hql.append("AND cre.id IN (:creatorIds) ");
			}
			if (rate != null) {
				hql.append("AND tas.rate = :rate ");
			}
			if (startDate != null) {
				hql.append("AND tas.startDate = :startDate ");
			}
			if (finishDate != null) {
				hql.append("AND tas.finishDate = :finishDate ");
			}
			hql.append("ORDER BY tas." + sort + " " + order);
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			query.setParameter("statusIds", statusIds);
			if(creatorIds.size()>0) {
				query.setParameter("creatorIds", creatorIds);
			}
			if (startDate != null) {
				query.setParameter("startDate", startDate);
			}
			if (finishDate != null) {
				query.setParameter("finishDate", finishDate);
			}
			if (rate != null) {
				query.setParameter("rate", rate);
			}
			if (departmentIds.size()==0 && offset!=-1 && limit!=-1) {
				query.setFirstResult(offset);
				query.setMaxResults(limit);
			}
			customTasks = new ArrayList<TaskModel>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Task task = (Task) obj[0];
				TaskModel customTask = new TaskModel();
				customTask.setId(task.getId());
				customTask.setTitle(task.getTitle());

				EmployeeModel creatorTemp = new EmployeeModel();
				creatorTemp.setId(task.getCreator().getId());
				creatorTemp.setCode(task.getCreator().getCode());
				creatorTemp.setName(task.getCreator().getName());
				creatorTemp.setAvatar(task.getCreator().getAvatar());
				creatorTemp.setGender(task.getCreator().getGender());
				creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
				creatorTemp.setEmail(task.getCreator().getEmail());
				creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
				creatorTemp.setActive(task.getCreator().isActive());
				creatorTemp.setCreateDate(task.getCreator().getCreateDate());
				customTask.setCreator(creatorTemp);

				EmployeeModel receiverTemp = new EmployeeModel();
				receiverTemp.setId(task.getReceiver().getId());
				receiverTemp.setCode(task.getReceiver().getCode());
				receiverTemp.setName(task.getReceiver().getName());
				receiverTemp.setAvatar(task.getReceiver().getAvatar());
				receiverTemp.setGender(task.getReceiver().getGender());
				receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
				receiverTemp.setEmail(task.getReceiver().getEmail());
				receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
				receiverTemp.setActive(task.getReceiver().isActive());
				receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
				customTask.setReceiver(receiverTemp);

				customTask.setCreateDate(task.getCreateDate());
				customTask.setFinishDate(task.getFinishDate());
				customTask.setModifyDate(task.getModifyDate());
				customTask.setStartDate(task.getStartDate());

				List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
				for (Department department : task.getCreator().getDepartments()) {
					DepartmentModel dModel = new DepartmentModel();
					dModel.setCode(department.getCode());
					dModel.setDescription(department.getDescription());
					dModel.setId(department.getId());
					dModel.setName(department.getName());
					dModel.setLevel(department.getLevel());
					departmentModels.add(dModel);
				}
				List<TaskHis> taskHis = taskHistoryRepositoryImpl.findById(customTask.getId());
				Collections.sort(taskHis, new Comparator<TaskHis>() {
					@Override
					public int compare(TaskHis o1, TaskHis o2) {
						return o2.getId().compareTo(o1.getId());
					}
				});

				List<TaskHisModelTemp> taskHisModelTemps = new ArrayList<TaskHisModelTemp>();
				for (TaskHis itemTaskHis : taskHis) {
					TaskHisModelTemp taskHisModel = new TaskHisModelTemp();
					taskHisModel.setId(itemTaskHis.getId());
					taskHisModel.setModifyDate(itemTaskHis.getModifyDate());
					taskHisModel.setStatus(itemTaskHis.getStatus());
					taskHisModel.setMessage(itemTaskHis.getMessage());
					
//					EmployeeModel receiverHis = new EmployeeModel();
//					receiverHis.setId(itemTaskHis.getReceiver().getId());
//					receiverHis.setCode(itemTaskHis.getReceiver().getCode());
//					receiverHis.setName(itemTaskHis.getReceiver().getName());
//					receiverHis.setAvatar(itemTaskHis.getReceiver().getAvatar());
//					receiverHis.setGender(itemTaskHis.getReceiver().getGender());
//					receiverHis.setDateOfBirth(itemTaskHis.getReceiver().getDateOfBirth());
//					receiverHis.setEmail(itemTaskHis.getReceiver().getEmail());
//					receiverHis.setPhoneNumber(itemTaskHis.getReceiver().getPhoneNumber());
//					receiverHis.setActive(itemTaskHis.getReceiver().isActive());
//					receiverHis.setCreateDate(itemTaskHis.getReceiver().getCreateDate());
//					taskHisModel.setReceiver(receiverHis);

					EmployeeModel editor = new EmployeeModel();
					editor.setId(itemTaskHis.getModifyBy().getId());
					editor.setCode(itemTaskHis.getModifyBy().getCode());
					editor.setName(itemTaskHis.getModifyBy().getName());
					editor.setAvatar(itemTaskHis.getModifyBy().getAvatar());
					editor.setGender(itemTaskHis.getModifyBy().getGender());
					editor.setDateOfBirth(itemTaskHis.getModifyBy().getDateOfBirth());
					editor.setEmail(itemTaskHis.getModifyBy().getEmail());
					editor.setPhoneNumber(itemTaskHis.getModifyBy().getPhoneNumber());
					editor.setActive(itemTaskHis.getModifyBy().isActive());
					editor.setCreateDate(itemTaskHis.getModifyBy().getCreateDate());
					taskHisModel.setModifyBy(editor);
					
					taskHisModel.setTaskId(itemTaskHis.getTaskId());

					taskHisModelTemps.add(taskHisModel);
				}

				List<TaskHisModel> taskHisModels = null;
				taskHisModels = new ArrayList<TaskHisModel>();
				for (TaskHisModelTemp itemHisModelTemp : taskHisModelTemps) {
					TaskHisModel taskHistoryModel = new TaskHisModel();
					taskHistoryModel.setMessage(itemHisModelTemp.getMessage());
					taskHistoryModel.setModifyBy(itemHisModelTemp.getModifyBy().getName());
					taskHistoryModel.setModifyDate(itemHisModelTemp.getModifyDate());
					taskHistoryModel.setModifyById(itemHisModelTemp.getModifyBy().getId());
					taskHistoryModel.setAvatar(itemHisModelTemp.getModifyBy().getAvatar());
					taskHisModels.add(taskHistoryModel);
				}
				customTask.setDepartment(departmentModels);
				customTask.setStatus(task.getStatus());
				customTask.setDescription(task.getDescription());
				customTask.setRate(task.getRate());
				customTask.setPriority(task.getPriority());
				customTask.setTaskHis(taskHisModels);
				if (departmentIds.size()==0) {
					customTasks.add(customTask);
				} else {
					boolean addFlag = false;
					for (DepartmentModel departmentModel : departmentModels) {
						for(Integer id : departmentIds) {
							if (departmentModel.getId() == id) {
								addFlag = true;
							}
						}

					}
					if (addFlag) {
						customTasks.add(customTask);
					}
				}

			}
			return customTasks;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	@Override
	public Integer countAllTaskAssigeToMe(Integer employeeId, List<Integer> creatorIds, List<Integer> departmentIds,
			List<Integer> statusIds, Integer rate, String startDate, String finishDate, String sort, String order,
			Integer offset, Integer limit) {
		try {
			Integer countTotal = 0;
			List<TaskModel> customTasks = null;
			Session session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder();
			hql.append("FROM tasks AS tas ");
			hql.append("INNER JOIN tas.creator AS cre ");
			hql.append("INNER JOIN tas.status AS st ");
			hql.append("INNER JOIN tas.receiver ");
			hql.append("WHERE tas.receiver.id = " + employeeId);
			hql.append(" AND st.id IN (:statusIds) ");
			if(creatorIds.size()>0) {
				hql.append("AND cre.id IN (:creatorIds) ");
			}
			if (rate != null) {
				hql.append("AND tas.rate = :rate ");
			}
			if (startDate != null) {
				hql.append("AND tas.startDate = :startDate ");
			}
			if (finishDate != null) {
				hql.append("AND tas.finishDate = :finishDate ");
			}
			Query query = session.createQuery(hql.toString());
			query.setParameter("statusIds", statusIds);
			if(creatorIds.size()>0) {
				query.setParameter("creatorIds", creatorIds);
			}
			if (startDate != null) {
				query.setParameter("startDate", startDate);
			}
			if (finishDate != null) {
				query.setParameter("finishDate", finishDate);
			}
			if (rate != null) {
				query.setParameter("rate", rate);
			}
//			if (departmentIds.size()==0) {
//				query.setFirstResult(offset);
//				query.setMaxResults(limit);
//			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Task task = (Task) obj[0];
				if (departmentIds.size()==0) {
					countTotal++;
				} else {
					boolean addFlag = false;
					for (Department department : task.getCreator().getDepartments()) {
						for(Integer id : departmentIds) {
							if (department.getId() == id) {
								addFlag = true;
							}
						}
					}
					if (addFlag) {
						countTotal++;
					}
				}
			}
			return countTotal;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return 0;
	}

	@Override
	public List<TaskModel> findAllTaskCreatedByMe(Integer employeeId, List<Integer> receiverIds,
			List<Integer> departmentIds, List<Integer> statusIds, Integer rate, String startDate, String finishDate,
			String sort, String order, Integer offset, Integer limit) {
		try {
			List<TaskModel> customTasks = null;
			Session session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder();
			hql.append("FROM tasks AS tas ");
			hql.append("INNER JOIN tas.creator AS cre ");
			hql.append("INNER JOIN tas.status AS st ");
			hql.append("INNER JOIN tas.receiver AS rec ");
			hql.append("WHERE cre.id = " + employeeId);
			hql.append(" AND st.id IN (:statusIds) ");
			if(receiverIds.size()>0) {
				hql.append("AND rec.id IN (:receiverIds) ");
			}
			if (rate != null) {
				hql.append("AND tas.rate = :rate ");
			}
			if (startDate != null) {
				hql.append("AND tas.startDate = :startDate ");
			}
			if (finishDate != null) {
				hql.append("AND tas.finishDate = :finishDate ");
			}
			hql.append("ORDER BY tas." + sort + " " + order);
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			query.setParameter("statusIds", statusIds);
			if(receiverIds.size()>0) {
				query.setParameter("receiverIds", receiverIds);
			}
			if (startDate != null) {
				query.setParameter("startDate", startDate);
			}
			if (finishDate != null) {
				query.setParameter("finishDate", finishDate);
			}
			if (rate != null) {
				query.setParameter("rate", rate);
			}
			if (departmentIds.size()==0 && offset!=-1 && limit !=-1) {
				query.setFirstResult(offset);
				query.setMaxResults(limit);
			}
			customTasks = new ArrayList<TaskModel>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Task task = (Task) obj[0];
				TaskModel customTask = new TaskModel();
				customTask.setId(task.getId());
				customTask.setTitle(task.getTitle());

				EmployeeModel creatorTemp = new EmployeeModel();
				creatorTemp.setId(task.getCreator().getId());
				creatorTemp.setCode(task.getCreator().getCode());
				creatorTemp.setName(task.getCreator().getName());
				creatorTemp.setAvatar(task.getCreator().getAvatar());
				creatorTemp.setGender(task.getCreator().getGender());
				creatorTemp.setDateOfBirth(task.getCreator().getDateOfBirth());
				creatorTemp.setEmail(task.getCreator().getEmail());
				creatorTemp.setPhoneNumber(task.getCreator().getPhoneNumber());
				creatorTemp.setActive(task.getCreator().isActive());
				creatorTemp.setCreateDate(task.getCreator().getCreateDate());
				customTask.setCreator(creatorTemp);

				EmployeeModel receiverTemp = new EmployeeModel();
				receiverTemp.setId(task.getReceiver().getId());
				receiverTemp.setCode(task.getReceiver().getCode());
				receiverTemp.setName(task.getReceiver().getName());
				receiverTemp.setAvatar(task.getReceiver().getAvatar());
				receiverTemp.setGender(task.getReceiver().getGender());
				receiverTemp.setDateOfBirth(task.getReceiver().getDateOfBirth());
				receiverTemp.setEmail(task.getReceiver().getEmail());
				receiverTemp.setPhoneNumber(task.getReceiver().getPhoneNumber());
				receiverTemp.setActive(task.getReceiver().isActive());
				receiverTemp.setCreateDate(task.getReceiver().getCreateDate());
				customTask.setReceiver(receiverTemp);

				customTask.setCreateDate(task.getCreateDate());
				customTask.setFinishDate(task.getFinishDate());
				customTask.setModifyDate(task.getModifyDate());
				customTask.setStartDate(task.getStartDate());

				List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
				for (Department department : task.getCreator().getDepartments()) {
					DepartmentModel dModel = new DepartmentModel();
					dModel.setCode(department.getCode());
					dModel.setDescription(department.getDescription());
					dModel.setId(department.getId());
					dModel.setName(department.getName());
					dModel.setLevel(department.getLevel());
					departmentModels.add(dModel);
				}
				List<TaskHis> taskHis = taskHistoryRepositoryImpl.findById(customTask.getId());
				Collections.sort(taskHis, new Comparator<TaskHis>() {
					@Override
					public int compare(TaskHis o1, TaskHis o2) {
						return o2.getId().compareTo(o1.getId());
					}
				});

				List<TaskHisModelTemp> taskHisModelTemps = new ArrayList<TaskHisModelTemp>();
				for (TaskHis itemTaskHis : taskHis) {
					TaskHisModelTemp taskHisModel = new TaskHisModelTemp();
					taskHisModel.setId(itemTaskHis.getId());
					taskHisModel.setModifyDate(itemTaskHis.getModifyDate());
					taskHisModel.setStatus(itemTaskHis.getStatus());
					taskHisModel.setMessage(itemTaskHis.getMessage());

					EmployeeModel editor = new EmployeeModel();
					editor.setId(itemTaskHis.getModifyBy().getId());
					editor.setCode(itemTaskHis.getModifyBy().getCode());
					editor.setName(itemTaskHis.getModifyBy().getName());
					editor.setAvatar(itemTaskHis.getModifyBy().getAvatar());
					editor.setGender(itemTaskHis.getModifyBy().getGender());
					editor.setDateOfBirth(itemTaskHis.getModifyBy().getDateOfBirth());
					editor.setEmail(itemTaskHis.getModifyBy().getEmail());
					editor.setPhoneNumber(itemTaskHis.getModifyBy().getPhoneNumber());
					editor.setActive(itemTaskHis.getModifyBy().isActive());
					editor.setCreateDate(itemTaskHis.getModifyBy().getCreateDate());
					taskHisModel.setModifyBy(editor);
					
					taskHisModel.setTaskId(itemTaskHis.getTaskId());

					taskHisModelTemps.add(taskHisModel);
				}

				List<TaskHisModel> taskHisModels = null;
				taskHisModels = new ArrayList<TaskHisModel>();
				for (TaskHisModelTemp itemHisModelTemp : taskHisModelTemps) {
					TaskHisModel taskHistoryModel = new TaskHisModel();
					taskHistoryModel.setMessage(itemHisModelTemp.getMessage());
					taskHistoryModel.setModifyBy(itemHisModelTemp.getModifyBy().getName());
					taskHistoryModel.setModifyDate(itemHisModelTemp.getModifyDate());
					taskHistoryModel.setModifyById(itemHisModelTemp.getModifyBy().getId());
					taskHistoryModel.setAvatar(itemHisModelTemp.getModifyBy().getAvatar());
					taskHisModels.add(taskHistoryModel);
				}
				customTask.setDepartment(departmentModels);
				customTask.setStatus(task.getStatus());
				customTask.setDescription(task.getDescription());
				customTask.setRate(task.getRate());
				customTask.setPriority(task.getPriority());
				customTask.setTaskHis(taskHisModels);
				if (departmentIds.size()==0) {
					customTasks.add(customTask);
				} else {
					boolean addFlag = false;
					for (DepartmentModel departmentModel : departmentModels) {
						for(Integer id : departmentIds) {
							if (departmentModel.getId() == id) {
								addFlag = true;
							}
						}

					}
					if (addFlag) {
						customTasks.add(customTask);
					}
				}

			}
			return customTasks;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	@Override
	public Integer countAllTaskCreatedByMe(Integer employeeId, List<Integer> receiverIds, List<Integer> departmentIds,
			List<Integer> statusIds, Integer rate, String startDate, String finishDate, String sort, String order,
			Integer offset, Integer limit) {
		try {
			Integer countTotal = 0;
			List<Integer> taskIds = null;
			Session session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder();
			hql.append("FROM tasks AS tas ");
			hql.append("INNER JOIN tas.creator AS cre ");
			hql.append("INNER JOIN tas.status AS st ");
			hql.append("INNER JOIN tas.receiver AS rec ");
			hql.append("WHERE cre.id = " + employeeId);
			hql.append(" AND st.id IN (:statusIds) ");
			if(receiverIds.size()>0) {
				hql.append("AND rec.id IN (:receiverIds)");
			}
			if (rate != null) {
				hql.append("AND tas.rate = :rate ");
			}
			if (startDate != null) {
				hql.append("AND tas.startDate = :startDate ");
			}
			if (finishDate != null) {
				hql.append("AND tas.finishDate = :finishDate");
			}
			Query query = session.createQuery(hql.toString());
			query.setParameter("statusIds", statusIds);
			if(receiverIds.size()>0) {
				query.setParameter("receiverIds", receiverIds);
			}
			if (startDate != null) {
				query.setParameter("startDate", startDate);
			}
			if (finishDate != null) {
				query.setParameter("finishDate", finishDate);
			}
			if (rate != null) {
				query.setParameter("rate", rate);
			}
//			if (departmentIds.size()==0) {
//				query.setFirstResult(offset);
//				query.setMaxResults(limit);
//			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Task task = (Task) obj[0];
				if (departmentIds.size()==0) {
					countTotal++;
				} else {
					boolean addFlag = false;
					for (Department department : task.getCreator().getDepartments()) {
						for(Integer id : departmentIds) {
							if (department.getId() == id) {
								addFlag = true;
							}
						}
					}
					if (addFlag) {
						countTotal++;
					}
				}
			}
			return countTotal;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return 0;
	}

	@Override
	public void ScanOverDueTask() {
		List<Integer> taskIds = null;
		StringBuilder hql = new StringBuilder("FROM tasks AS t ");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Task task = (Task) obj;
				DateTimeFormatter dtf = null;
				LocalDate now = LocalDate.now();
//				dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate dt = LocalDate.parse(task.getFinishDate(), dtf);
				if(now.isAfter(dt)) {
					Status status = new Status();
					status = statusRepositotyImpl.findByIndexAndType(6, "task");
					task.setStatus(status);
					session.update(task);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in findAll() ", e);
		}
	}

	@Override
	public List<StatusModel> countTaskByStatus() {
		StringBuilder hql = new StringBuilder("FROM tasks AS t ");
		List<StatusModel> statusModels = new ArrayList<>();
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			List<Status> statuses = statusRepositotyImpl.findAllForTask();
			List<Task> tasks = new ArrayList<Task>();

			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Task task = (Task) obj;
				tasks.add(task);

			}
			for(Status status : statuses) {
				int count =0;
				for(Task item : tasks) {
					if(item.getStatus().getId() == status.getId()) {
						count++;
					}
				}
				StatusModel statusModel = new StatusModel();
				statusModel.setId(status.getId());
				statusModel.setName(status.getName());
				statusModel.setCountByStatus(count);
				statusModels.add(statusModel);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in findAll() ", e);
		}
		return statusModels;
	}

}

class TaskComparator implements Comparator<Department> {
	// Overriding compare()method of Comparator
	// for descending order of cgpa
	public int compare(Department d1, Department d2) {
		if (d1.getLevel() < d2.getLevel()) {
			return 1;
		} else if (d1.getLevel() > d2.getLevel()) {
			return -1;
		}
		return 0;
	}
}

