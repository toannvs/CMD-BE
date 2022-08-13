package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.ApprovalOption_View;
import com.comaymanagement.cmd.repository.IApprovalOption_ViewRepository;

@Repository
@Transactional(rollbackFor = Exception.class)
public class ApprovalOption_ViewRepository implements IApprovalOption_ViewRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalOption_ViewRepository.class);
	@Autowired
	private SessionFactory sessionFactory;

	public List<ApprovalOption_View> findAll(String name) {
		List<ApprovalOption_View> appApprovalOption_Views = new ArrayList<>();
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		hql.append("from v_approval_options as app_pro ");
		if(!name.equals("") && name!=null) {
			hql.append("where app_pro.name like :name ");
		}
		try {
			Query query = session.createQuery(hql.toString());
			if(!name.equals("") && name!=null ) {
			query.setParameter("name","%" + name + "%");
			}
			LOGGER.info(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				ApprovalOption_View approvalOption = (ApprovalOption_View)it.next();
				appApprovalOption_Views.add(approvalOption);
			}
			return appApprovalOption_Views;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}

	}
	public ApprovalOption_View findById(Integer id, String table) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		hql.append("from v_approval_options app_option ");
		hql.append("where app_option.id = :id and app_option.table = :table ");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
			query.setParameter("table", table);
			LOGGER.info(hql.toString());
			ApprovalOption_View approvalOption = (ApprovalOption_View) query.getSingleResult();
			return approvalOption;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
}
