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

import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.model.ProposalTypeModel;
import com.comaymanagement.cmd.repository.IProposalTypeRepository;

import net.bytebuddy.asm.Advice.This;
@Repository
@Transactional(rollbackFor = Exception.class)
public class ProposalTypeRepositoryImpl implements IProposalTypeRepository{
	private static final Logger LOGGER = LoggerFactory.getLogger(This.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public ProposalType findById(String id) {
		ProposalType proposalType = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder();
			hql.append("FROM proposal_types AS pt WHERE pt.id = :id");
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", Integer.valueOf(id));
			proposalType = (ProposalType) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return proposalType;
	}
	
	public List<ProposalType> findAll() {
		StringBuilder hql = new StringBuilder();
		hql.append("from proposal_types");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			List<ProposalType> poposalTypes = new ArrayList<>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				ProposalType proposalType = (ProposalType) it.next();
				poposalTypes.add(proposalType);
			}
			return poposalTypes;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
	
	public List<ProposalType> findProposalPermission(Integer employeeId, List<Integer> positions,
			List<Integer> departments) {
		StringBuilder hql = new StringBuilder();
		List<ProposalType> proposalTypes = new ArrayList<>();
		// Checl if employeeId cannot found => check employeeId in department and
		// position
//	    String listStatus = status.stream().map(stat -> "'" + stat + "'").collect(Collectors.joining(","));

		hql.append("from proposal_types proType ");
		hql.append("inner join proType.proposalPermissions as proPer ");
		hql.append("where proPer.employeeId = :employeeId ");
		if (positions != null && positions.size() > 0) {
			hql.append("or proPer.positionId IN (:positions) ");
		}

		if (departments != null && departments.size() > 0) {
			hql.append("or proPer.departmentId IN (:departments) ");
		}
		hql.append("order by proType.id asc ");
		LOGGER.info(hql.toString());
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("employeeId", employeeId);
			if (positions != null && positions.size() > 0) {

				query.setParameter("positions", positions);
			}
			if (departments != null && departments.size() > 0) {
				query.setParameter("departments", departments);
			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] objects = (Object[]) it.next();
				ProposalType proposalType = (ProposalType) objects[0];
				proposalTypes.add(proposalType);
			}

		} catch (Exception e) {
			LOGGER.error(e.toString());
			e.printStackTrace();
			return null;
		}
		return proposalTypes;
	}
	
	// if not select anyone for permission create
	public List<ProposalType> findProposalEnableAll(){
		List<ProposalType> proposalTypes = new ArrayList<>();
		StringBuilder hql = new StringBuilder();
		hql.append("from proposal_types proType ");
		hql.append("where proType.id not in ");
		hql.append("(select proPer.proposalType.id from proposals_permissions as proPer) ");
		hql.append("order by proType.id asc");
		LOGGER.info(hql.toString());
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				ProposalType proposalType = (ProposalType) it.next();
				proposalTypes.add(proposalType);
			}
			return proposalTypes;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		
	}
	public ProposalTypeModel toModel (ProposalType proposalType) {
		ProposalTypeModel model = new ProposalTypeModel();
		model.setId(proposalType.getId());
		model.setName(proposalType.getName());
		model.setCreateDate(proposalType.getCreateDate());
		model.setActiveFlag(proposalType.isActiveFlag());
		return model;
	}
}
