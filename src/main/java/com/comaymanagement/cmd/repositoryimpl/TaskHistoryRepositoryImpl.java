package com.comaymanagement.cmd.repositoryimpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.entity.TaskHis;
import com.comaymanagement.cmd.repository.ITaskHistory;

import net.bytebuddy.asm.Advice.This;

@Repository
@Transactional
public class TaskHistoryRepositoryImpl implements ITaskHistory {

	static final Logger LOGGER = LoggerFactory.getLogger(This.class); 
	
	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public List<TaskHis> add(List<TaskHis> taskHis) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String modifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			taskHis.get(0).setModifyDate(modifyDate);;
			Integer id =(Integer) session.save(taskHis);
			if(id != -1) {
				taskHis.get(0).setId(id);
				return taskHis;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TaskHis> findById(Integer id) {
		List<TaskHis> taskHis = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "FROM task_his AS th WHERE th.id = " + id;
			Query query = session.createQuery(hql);
			taskHis = (List<TaskHis>) query.getResultList();
			if(null != taskHis) {
				return taskHis;
			}

			
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

}
