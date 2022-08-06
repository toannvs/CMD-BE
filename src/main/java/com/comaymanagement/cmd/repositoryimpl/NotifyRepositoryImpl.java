package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.constant.CMDConstrant;
import com.comaymanagement.cmd.entity.Notify;
import com.comaymanagement.cmd.model.NotifyModel;
import com.comaymanagement.cmd.repository.INotifyRepository;

import net.bytebuddy.asm.Advice.This;

@Repository
public class NotifyRepositoryImpl implements INotifyRepository {

	static final Logger LOGGER = LoggerFactory.getLogger(This.class);

	@Autowired
	SessionFactory sessionFactory;

	@Override
	public List<NotifyModel> findByEmployeeId(Integer employeeId, String keySearch, Integer offset, Integer limit,
			String sort, String order) {
		Session session = null;
		List<NotifyModel> notifyModels = null;
		List<Notify> notifies = null;
		try {

			StringBuilder hql = new StringBuilder();
			hql.append("FROM notify as no WHERE no.receiver.id = " + employeeId);
			if (null != keySearch && !keySearch.equals("")) {
				hql.append(" and no.description LIKE CONCAT('%',:keySearch,'%')");
			}
			hql.append(" order by no." + sort + " " + order);
			LOGGER.debug(hql.toString());
			session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			if (null != keySearch && !keySearch.equals("")) {
				query.setParameter("keySearch", keySearch);
			}
			query.setFirstResult(offset);
			query.setMaxResults(limit);
			notifies = new ArrayList<Notify>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Notify notify = (Notify) obj;
				notifies.add(notify);
			}
			notifyModels = new ArrayList<NotifyModel>();
			if (null != notifies && notifies.size() > 0) {
				for (Notify itemNotify : notifies) {
					NotifyModel notifyModel = new NotifyModel();
					notifyModel.setId(itemNotify.getId());
					notifyModel.setDescription(itemNotify.getDescription());
					notifyModel.setIsRead(itemNotify.getIsRead());
					notifyModel.setTitle(itemNotify.getTitle());
					notifyModel.setReceiverId(itemNotify.getReceiver().getId());
					notifyModels.add(notifyModel);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return notifyModels;
	}

	@Override
	public Integer add(Notify notify) {
		Session session = null;
		Integer result = CMDConstrant.FAILED;
		try {
			session = sessionFactory.getCurrentSession();
			result = (Integer) session.save(notify);
			if (result != CMDConstrant.FAILED) {
				return result;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	@Override
	public boolean allRead(Integer employeeId, List<Integer> notifyIds) {
		Session session = null;
		List<Notify> notifies = null;
		try {
			session = sessionFactory.getCurrentSession();

			notifies = findByEmployeeIdToEdit(employeeId);
			if (null != notifyIds && notifyIds.size() == 0) {
				for (Notify item : notifies) {
					item.setIsRead(true);
					session.update(item);
				}
				return true;
			} else {
				for (Notify item : notifies) {
					for (Integer id : notifyIds) {
						if (item.getId() == id) {
							item.setIsRead(true);
							session.update(item);
						}
					}
				}
				return true;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return false;
	}

	@Override
	public boolean delete(List<Integer> notifyIds) {
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			for (Integer id : notifyIds) {
				Notify notify = findById(id);
				session.delete(notify);
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return false;
	}

	@Override
	public Notify findById(Integer notifyId) {
		Session session = null;
		Notify notify = null;
		try {
			session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder();
			hql.append("FROM notify as no WHERE no.id = " + notifyId);
			Query query = session.createQuery(hql.toString());
			notify = (Notify) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return notify;
	}

	@Override
	public List<Notify> findByEmployeeIdToEdit(Integer employeeId) {
		Session session = null;
		List<Notify> notifies = null;
		try {
			session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder();
			hql.append("FROM notify as no WHERE no.receiver.id = " + employeeId);
			Query query = session.createQuery(hql.toString());
			notifies = new ArrayList<Notify>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Notify notify = (Notify) obj;
				notifies.add(notify);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return notifies;
	}

}
