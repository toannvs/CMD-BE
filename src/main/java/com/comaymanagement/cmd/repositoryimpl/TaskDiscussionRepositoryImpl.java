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
import com.comaymanagement.cmd.entity.Notify;
import com.comaymanagement.cmd.entity.TaskDiscussion;
import com.comaymanagement.cmd.entity.TaskHis;
import com.comaymanagement.cmd.model.NotifyModel;
import com.comaymanagement.cmd.model.TaskDiscussionModel;
import com.comaymanagement.cmd.repository.ITaskDiscussionRepository;
@Repository
@Transactional
public class TaskDiscussionRepositoryImpl implements ITaskDiscussionRepository{
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskDiscussionRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	public Integer add(TaskDiscussion taskDiscussion) {
		Session session = null;
		Integer result = CMDConstrant.FAILED;
		try {
			session = sessionFactory.getCurrentSession();
			result = (Integer) session.save(taskDiscussion);
			if (result != CMDConstrant.FAILED) {
				return result;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}
	public List<TaskDiscussionModel> findByTaskId(Integer id) {
		List<TaskDiscussionModel> taskDiscussionModels = new ArrayList<>();
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "FROM task_discussions AS td WHERE td.task.id = " + id;
			Query query = session.createQuery(hql);
			taskDiscussionModels = new ArrayList<TaskDiscussionModel>();
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				TaskDiscussion taskDis = new TaskDiscussion();
				Object obj = (Object) it.next();
				taskDis =(TaskDiscussion) obj;
				taskDiscussionModels.add(toModel(taskDis));
				
			}
			if(null != taskDiscussionModels) {
				return taskDiscussionModels;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return taskDiscussionModels;
		}
		return taskDiscussionModels;
	}
	public TaskDiscussionModel toModel(TaskDiscussion taskDiscussion) {
		TaskDiscussionModel taskDiscussionModel = new TaskDiscussionModel();
		taskDiscussionModel.setContent(taskDiscussion.getContent());
		Employee modifyBy = taskDiscussion.getModifyBy();
		taskDiscussionModel.setModifyBy(modifyBy.getName());
		taskDiscussionModel.setModifyById(modifyBy.getId());
		taskDiscussionModel.setAvatar(modifyBy.getAvatar());
		taskDiscussionModel.setModifyDate(taskDiscussion.getModifyDate());
		return taskDiscussionModel;
	}
}
