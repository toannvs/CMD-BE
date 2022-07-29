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

import com.comaymanagement.cmd.entity.ApprovalStepDetail;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.repository.IApprovalStepDetailRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class ApprovalStepDetailRepositoryImpl implements IApprovalStepDetailRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalStepDetailRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	
	public List<ApprovalStepDetail> findAllByApprovalStepId(Integer approvalStepId){
		 List<ApprovalStepDetail> approvalStepDetails = new ArrayList<>();
			StringBuilder hql = new StringBuilder("FROM approval_step_details app_step_detail WHERE app_step_detail.approvalStep.id = :approvalStepId");
			try {
				Session session = sessionFactory.getCurrentSession();
				Query query = session.createQuery(hql.toString());
				LOGGER.info(hql.toString());
				query.setParameter("approvalStepId", approvalStepId);
				for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
					ApprovalStepDetail app = (ApprovalStepDetail) it.next();
					approvalStepDetails.add(app);
				}
				return approvalStepDetails;
			} catch (Exception e) {
				LOGGER.error("Error has occured in findAllByDepartmentId() ", e);
				return null;
			}
	}
}
