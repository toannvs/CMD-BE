package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.constant.CMDConstrant;
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.RoleDetail;
import com.comaymanagement.cmd.entity.TaskDiscussion;
import com.comaymanagement.cmd.entity.TaskReminder;
import com.comaymanagement.cmd.model.TaskDiscussionModel;
import com.comaymanagement.cmd.model.TaskReminderModel;
import com.comaymanagement.cmd.repository.ITaskReminderRepository;
@Repository
@Transactional
public class TaskReminderRepositoryImpl implements ITaskReminderRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReminderRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	public List<TaskReminderModel> findByTaskId(Integer taskId, Integer empId) {
		List<TaskReminderModel> taskReminders = new ArrayList<>();
		try {
			Session session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder("FROM task_reminders AS tr WHERE tr.task.id = :taskId ");
			hql.append("AND tr.modifyBy.id = : empId");
			Query query = session.createQuery(hql.toString());
			query.setParameter("taskId", taskId);
			query.setParameter("empId", empId);
			taskReminders = new ArrayList<TaskReminderModel>();
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				TaskReminder taskRemd = new TaskReminder();
				Object obj = (Object) it.next();
				taskRemd =(TaskReminder) obj;
				taskReminders.add(toModel(taskRemd));
			}
			if(null != taskReminders) {
				return taskReminders;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return taskReminders;
		}
		return taskReminders;
	} 
	public Integer add(TaskReminder taskReminder) {
		Session session = null;
		Integer result = CMDConstrant.FAILED;
		try {
			session = sessionFactory.getCurrentSession();
			result = (Integer) session.save(taskReminder);
			if (result != CMDConstrant.FAILED) {
				return result;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}
	public Integer edit(TaskReminder taskReminder) {
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			session.update(taskReminder);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured in at edit() ", e);
			return -1;
		}
	}
	public Integer delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			TaskReminder taskReminder = new TaskReminder();
			taskReminder = session.find(TaskReminder.class, id);
			session.remove(taskReminder);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return -1;
		}
	}
	public TaskReminder findById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		TaskReminder taskReminder = null;
		StringBuilder hql = new StringBuilder();
		hql.append("FROM task_reminders tr where tr.id = :id");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
			taskReminder = (TaskReminder) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Have error at findById(): ",e);
		}
		return taskReminder;
	}
	public TaskReminderModel toModel(TaskReminder taskReminder) {
		TaskReminderModel taskReminderModel = new TaskReminderModel();
		taskReminderModel.setId(taskReminder.getId());
		taskReminderModel.setTaskId(taskReminder.getTask().getId());
		taskReminderModel.setTime(taskReminder.getTime());
		taskReminderModel.setTimeRemaining(taskReminder.getTimeRemaining());
		return taskReminderModel;
	}
}
