package com.comaymanagement.cmd.repositoryimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	public Integer add(TaskHis taskHis) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String modifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			taskHis.setModifyDate(modifyDate);;
			Integer id =(Integer) session.save(taskHis);
			if(id != -1) {
				taskHis.setId(id);
				return id;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	@Override
	public List<TaskHis> findById(Integer id) {
		List<TaskHis> taskHistories = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "FROM task_his AS th WHERE th.taskId = " + id;
			Query query = session.createQuery(hql);
			taskHistories = new ArrayList<TaskHis>();
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				TaskHis taskHis = new TaskHis();
				Object obj = (Object) it.next();
				taskHis =(TaskHis) obj;
				taskHistories.add(taskHis);
				
			}
			if(null != taskHistories) {
				return taskHistories;
			}

			
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

}
