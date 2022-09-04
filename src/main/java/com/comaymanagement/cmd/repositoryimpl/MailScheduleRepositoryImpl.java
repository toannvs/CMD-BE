package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.entity.MailSchedule;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.repository.MailScheduleRepository;
@Repository
@Transactional
public class MailScheduleRepositoryImpl implements MailScheduleRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(MailScheduleRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	@Override
	public MailSchedule findByScheduleIdAndIsDeletedFalse(Integer scheduleId) {
		StringBuilder hql = new StringBuilder();
		hql.append("FROM mail_schedule as ms where ms.scheduleId = :scheduleId ");
		hql.append("and ms.isDeleted = false");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("scheduleId", scheduleId);
			MailSchedule result = (MailSchedule) query.getSingleResult();
			return result;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	@Override
	public List<MailSchedule> findByUsernameAndIsDeletedFalse(String username) {
		StringBuilder hql = new StringBuilder();
		List<MailSchedule> mailSchedules = new ArrayList<>();
		hql.append("FROM mail_schedule as ms where ms.username = :username ");
		hql.append("and ms.isDeleted = false");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("username", username);
			MailSchedule result = (MailSchedule) query.getSingleResult();
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				Object obj  = (Object) it.next();
				MailSchedule mailSchedule = (MailSchedule) obj;
				mailSchedules.add(mailSchedule);
			}
			return mailSchedules;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return mailSchedules;
		}
	}

	@Override
	public boolean existsByScheduleIdAndIsDeletedFalse(Integer scheduleId) {
		StringBuilder hql = new StringBuilder();
		hql.append("FROM mail_schedule as ms where ms.scheduleId = :scheduleId ");
		hql.append("and ms.isDeleted = false");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("scheduleId", scheduleId);
			MailSchedule result = (MailSchedule) query.getSingleResult();
			if(result!=null) {
				return true;
			}else {
				return false;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	@Override
	public MailSchedule save(MailSchedule mailSchedule) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.save(mailSchedule);
			return mailSchedule;
		} catch (Exception e) {
			LOGGER.error("Error has occured in addEmployee() ", e);
			return null;
		}
	}
	@Override
	public MailSchedule findById(Integer scheduleId) {
		StringBuilder hql = new StringBuilder();
		hql.append("FROM mail_schedule as ms where ms.scheduleId = :scheduleId ");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("scheduleId", scheduleId);
			MailSchedule result = (MailSchedule) query.getSingleResult();
			return result;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
}
