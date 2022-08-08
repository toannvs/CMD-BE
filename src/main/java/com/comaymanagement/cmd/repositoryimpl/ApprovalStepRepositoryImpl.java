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

import com.comaymanagement.cmd.entity.ApprovalStep;
import com.comaymanagement.cmd.model.ApprovalStepModel;
import com.comaymanagement.cmd.repository.IApprovalStepRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class ApprovalStepRepositoryImpl implements IApprovalStepRepository{
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	@Override
	public List<ApprovalStep> findByProposalTypeId(Integer proposalTypeId) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		List<ApprovalStep> approvalSteps = new ArrayList<>();
		hql.append("from approval_steps app_step ");
		hql.append("where app_step.proposalType.id = :proposalTypeId ");
		hql.append("order by app_step.approvalStepIndex asc");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("proposalTypeId", proposalTypeId);
			LOGGER.info(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				ApprovalStep approvalStep = (ApprovalStep) it.next();
				approvalSteps.add(approvalStep);
			}
			return approvalSteps;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
	public List<ApprovalStep> findByProposalTypeIdAndIndex(Integer proposalTypeId, String step) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		List<ApprovalStep> approvalSteps = new ArrayList<>();
		hql.append("from approval_steps app_step ");
		hql.append("where app_step.proposalType.id = :proposalTypeId ");
		hql.append("and app_step.approvalStepIndex >=  :step ");
		hql.append("order by app_step.approvalStepIndex asc");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("proposalTypeId", proposalTypeId);
			query.setParameter("step", Integer.valueOf(step));
			LOGGER.info(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				ApprovalStep approvalStep = (ApprovalStep) it.next();
				approvalSteps.add(approvalStep);
			}
			return approvalSteps;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
	public Integer countByProposalTypeId(Integer proposalTypeId) {
		Session session = sessionFactory.getCurrentSession();
		Integer result = 0;
		StringBuilder hql = new StringBuilder();
		hql.append("select count(*) from approval_steps app_step ");
		hql.append("where app_step.proposalType.id = :proposalTypeId ");
		hql.append("order by app_step.approvalStepIndex asc");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("proposalTypeId", proposalTypeId);
			LOGGER.info(hql.toString());
				result =Integer.valueOf(query.getSingleResult().toString()) ;
			return result;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return result;
		}
	}
	public ApprovalStep findById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		hql.append("from approval_steps app_step ");
		hql.append("where app_step.id = :id ");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
			LOGGER.info(hql.toString());
			ApprovalStep approvalStep = (ApprovalStep) query.getSingleResult();
			return approvalStep;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
	public Integer add(ApprovalStep appro) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Integer id = (Integer) session.save(appro);
			return id;
		} catch (Exception e) {
			LOGGER.error("Error has occured at add() ", e);
		}
		return -1;
	}
	public Integer edit(ApprovalStep appro) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(appro);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured at edit() ", e);
			return 0;
		}
	}
	public Integer delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			ApprovalStep appro = new ApprovalStep();
			appro = session.find(ApprovalStep.class, id);
			session.remove(appro);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured at delete() ", e);
			return 0;
		}

	}
	public List<ApprovalStepModel> toModel(List<ApprovalStep> approvalSteps){
		List<ApprovalStepModel> approvalStepModels = new ArrayList<>();
		for(ApprovalStep approvalStep : approvalSteps) {
			ApprovalStepModel approvalStepModel = new ApprovalStepModel();
			approvalStepModel.setId(approvalStep.getId());
			approvalStepModel.setIndex(approvalStep.getApprovalStepIndex());
			approvalStepModel.setName(approvalStep.getApprovalStepName());
			approvalStepModel.setApprovalConfigTargets(new ArrayList<>());
			approvalStepModels.add(approvalStepModel);
		}
		return approvalStepModels;
	}
}
