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

import com.comaymanagement.cmd.entity.ProposalPermission;
import com.comaymanagement.cmd.repository.IProposalPermissionRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class ProposalPermissionImpl implements IProposalPermissionRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProposalPermissionImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	public List<ProposalPermission> findAllByProposalTypeId(Integer proposalTypeId){
		StringBuilder hql = new StringBuilder();
		hql.append("from proposals_permissions proPer ");
		hql.append("where proPer.proposalType.id = :proposalTypeId");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("proposalTypeId", proposalTypeId);
			List<ProposalPermission> proposalPermissions = new ArrayList<>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				ProposalPermission proposalPermission = (ProposalPermission) it.next();
				proposalPermissions.add(proposalPermission);
			}
			return proposalPermissions;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		
	}
	public Integer delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			ProposalPermission proposalPermission = new ProposalPermission();
			proposalPermission = session.find(ProposalPermission.class, id);
			session.remove(proposalPermission);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured at delete() ", e);
			return -1;
		}

	}
	public Integer delete(ProposalPermission proposalPermission) {
		Session session = sessionFactory.getCurrentSession();
		try {
//			ProposalPermission proposalPermission = new ProposalPermission();
//			proposalPermission = session.find(ProposalPermission.class, id);
			session.remove(proposalPermission);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured at delete() ", e);
			return -1;
		}
		
	}
	public Integer add(ProposalPermission proposalPermission) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Integer id = (Integer) session.save(proposalPermission);
			return id;
		} catch (Exception e) {
			LOGGER.error("Error has occured at add() ", e);
		}
		return -1;
	}
}
