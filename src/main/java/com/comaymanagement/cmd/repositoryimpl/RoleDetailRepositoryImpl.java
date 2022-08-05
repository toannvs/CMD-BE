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
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.RoleDetail;
import com.comaymanagement.cmd.repository.IRoleDetailRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class RoleDetailRepositoryImpl implements IRoleDetailRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleRepositoryImpl.class);

	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public Integer add(RoleDetail roleDetail) {
		Session session = sessionFactory.getCurrentSession();
		try {
			return Integer.parseInt(session.save(roleDetail).toString());
		} catch (Exception e) {
			LOGGER.error("Error has occured in addEmployee() ", e);
			return -1;
		}
	}

	@Override
	public Integer delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			RoleDetail roleDetail = new RoleDetail();
			roleDetail = session.find(RoleDetail.class, id);
			session.remove(roleDetail);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return -1;
		}
	}

	@Override
	public List<RoleDetail> findAllByRoleId(Integer roleId) {
		Session session = sessionFactory.getCurrentSession();
		List<RoleDetail> roleDetails = null;

		StringBuilder hql = new StringBuilder();
		hql.append("FROM role_details as rd where rd.roleId = " + roleId);
		try {
			Query query = session.createQuery(hql.toString());
//			query.setParameter("id", roleId);
			roleDetails = new ArrayList<>();
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				RoleDetail roleDetail = (RoleDetail) it.next();
				roleDetails.add(roleDetail);
			}
		} catch (Exception e) {
			LOGGER.error("Have error at findAllByRoleId(): ",e);
		}
		return roleDetails;
	}

}
