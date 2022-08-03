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

import com.comaymanagement.cmd.entity.Status;
import com.comaymanagement.cmd.repository.IStatusRepositoty;

import net.bytebuddy.asm.Advice.This;
@Repository
@Transactional
/**
The Desciption of the method to explain what the method does
@param the parameters used by the method
@return the value returned by the method
@throws what kind of exception does this method throw
*/
public class StatusRepositotyImpl implements IStatusRepositoty {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(This.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public Status findById(Integer id) {
		Status status = null;
		StringBuilder hql = new StringBuilder("FROM statuses AS st ");
		hql.append("WHERE st.id = " + id);
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			status = (Status) query.getSingleResult();

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return status;
	}

	@Override
	public List<Status> findAllForTask() {
		
		List<Status> statuses = null;
		StringBuilder hql = new StringBuilder();
		hql.append("FROM statuses AS st ");
		hql.append("where st.type = :type ");
		hql.append("order by st.index asc");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("type", "task");
			statuses = new ArrayList<>();
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				Object object = it.next();
				Status status = new Status();
				status = (Status) object;
				statuses.add(status);
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return statuses;
	}
	public List<Status> findAllForProposal() {
		List<Status> statuses = null;
		StringBuilder hql = new StringBuilder();
		hql.append("FROM statuses AS st ");
		hql.append("where st.type = :type ");
		hql.append("order by st.index asc");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("type", "proposal");
			statuses = new ArrayList<>();
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				Object object = it.next();
				Status status = new Status();
				status = (Status) object;
				statuses.add(status);
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return statuses;
	}
	public Status findByIndexAndType(Integer index, String type) {
		Status status = null;
		StringBuilder hql = new StringBuilder("FROM statuses AS st ");
		hql.append("WHERE st.index = :index ");
		hql.append("AND st.type = :type ");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("index", "index");
			query.setParameter("type", "type");
			LOGGER.info(hql.toString());
			status = (Status) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return status;
	}
}
