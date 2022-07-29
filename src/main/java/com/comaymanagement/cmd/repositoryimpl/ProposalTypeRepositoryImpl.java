package com.comaymanagement.cmd.repositoryimpl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.ProposalType;
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
			Query<?> query = session.createQuery(hql.toString());
			query.setParameter("id", Integer.valueOf(id));
			proposalType = (ProposalType) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return proposalType;
	}
	
	public ProposalType findAll(String id) {
		ProposalType proposalType = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder();
			hql.append("FROM proposal_types AS pt WHERE ");
			Query<?> query = session.createQuery(hql.toString());
			query.setParameter("id", Integer.valueOf(id));
			proposalType = (ProposalType) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return proposalType;
	}
}
