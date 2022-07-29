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
public class StatusRepositotyImpl implements IStatusRepositoty {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(This.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public Status findById(Integer id) {
		Status status = null;
		StringBuilder hql = new StringBuilder("FROM statuses AS st ");
		hql.append("WHERE st.id = :id");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("id",id);			
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				status = (Status) it.next();
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return status;
	}

	@Override
	public List<Status> findAll() {
		List<Status> statuses = null;
		String hql = "FROM statuses AS st";
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql);
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

}
