package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.ApprovalStep;
import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.Proposal;
import com.comaymanagement.cmd.entity.ProposalDetail;
import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.Status;
import com.comaymanagement.cmd.model.ContentModel;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.model.EmployeeModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.ProposalModel;
import com.comaymanagement.cmd.repository.IProposalRepository;

import net.bytebuddy.asm.Advice.This;

@Repository
@Transactional(rollbackFor = Exception.class)
public class ProposalRepositoryImpl implements IProposalRepository {

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	DepartmentRepositoryImpl departmentRepository;

	@Autowired
	PositionRepositoryImpl positionRepository;

	private final Logger LOGGER = LoggerFactory.getLogger(This.class);
	
	// proposals

	public List<ProposalModel> findAllProposalForAll(Integer proposalTypeId, 
			List<Integer> statusIds, Integer creator, String createDateFrom, String createDateTo, String sort, String order,
			Integer offset, Integer limit) {
		List<Proposal> proposals = new ArrayList<>();
		List<ProposalModel> proposalModelResult = new ArrayList<>();
		Set<Proposal> proposalsTMP = new LinkedHashSet();
		proposalsTMP = findAllForAll(proposalTypeId, statusIds, creator, createDateFrom,
				createDateTo, sort, order);
		// store proposal of each proposalType and step
		if (proposalsTMP != null && proposalsTMP.size() > 0) {
			for (Proposal pro : proposalsTMP) {
				proposals.add(pro);
			}
		}
		// paging
		for (int i = offset; i < proposals.size() && proposalModelResult.size() < limit; i++) {
			ProposalModel proposalModel = this.findById(proposals.get(i).getId());
			proposalModelResult.add(proposalModel);
		}
		return proposalModelResult;
	}
	
	@Override
	public List<ProposalModel> findAllProposalApproveByMe(Integer employeeId, Integer proposalTypeId, 
			List<Integer> statusIds, Integer creator, String createDateFrom, String createDateTo, String sort, String order,
			Integer offset, Integer limit) {
		List<ApprovalStep> appSteps = new ArrayList<>();
		List<Department> departments = new ArrayList<>();
		List<Position> positions = new ArrayList<>();
		List<Proposal> proposals = new ArrayList<>();
		List<ProposalModel> proposalModelResult = new ArrayList<>();
		positions = positionRepository.findAllByEmployeeId(employeeId);
		departments = departmentRepository.findAllByEmployeeId(employeeId);
		appSteps = findApprovalStepDetail(employeeId, positions, departments);

		for (ApprovalStep appStep : appSteps) {
			// Check if fillter with proposal type id
			if (proposalTypeId != null) {
				if (proposalTypeId == appStep.getProposalType().getId()) {
					String step = appStep.getApprovalStepIndex();
					Set<Proposal> proposalsTMP = new LinkedHashSet();
					proposalsTMP = findAllApproveByMe(proposalTypeId, statusIds, creator, createDateFrom,
							createDateTo, step, sort, order);
					// store proposal of each proposalType and step
					if (proposalsTMP != null && proposalsTMP.size() > 0) {
						for (Proposal pro : proposalsTMP) {
							proposals.add(pro);
						}
					}
				}
			} else {
				Integer currentEmpProposalTypeId = appStep.getProposalType().getId();
				String step = appStep.getApprovalStepIndex();
				Set<Proposal> proposalsTMP = new LinkedHashSet();
				proposalsTMP = findAllApproveByMe(currentEmpProposalTypeId, statusIds, creator, createDateFrom,
						createDateTo, step, sort, order);
				// store proposal of each proposalType and step
				if (proposalsTMP != null && proposalsTMP.size() > 0) {
					for (Proposal pro : proposalsTMP) {
						proposals.add(pro);
					}
				}
			}

		}
		// paging
		for (int i = offset; i < proposals.size() && proposalModelResult.size() < limit; i++) {
			ProposalModel proposalModel = this.findById(proposals.get(i).getId());
			proposalModelResult.add(proposalModel);
		}
		return proposalModelResult;
	}
	public List<ProposalModel> findAllProposalCratedByMe(Integer employeeId, Integer proposalTypeId, 
			List<Integer> statusIds, Integer creator, String createDateFrom, String createDateTo, String sort, String order,
			Integer offset, Integer limit) {
		List<Proposal> proposals = new ArrayList<>();
		List<ProposalModel> proposalModelResult = new ArrayList<>();
		Set<Proposal> proposalsTMP = new LinkedHashSet();
		proposalsTMP = findAllCreatedByMe(employeeId,proposalTypeId, statusIds, creator, createDateFrom,
				createDateTo, sort, order);
		// store proposal of each proposalType and step
		if (proposalsTMP != null && proposalsTMP.size() > 0) {
			for (Proposal pro : proposalsTMP) {
				proposals.add(pro);
			}
		}
		// paging
		for (int i = offset; i < proposals.size() && proposalModelResult.size() < limit; i++) {
			ProposalModel proposalModel = this.findById(proposals.get(i).getId());
			proposalModelResult.add(proposalModel);
		}
		return proposalModelResult;
	}

	public boolean checkExitsInStep(String employeeId) {
		// Check all record of approvalStepDetail is exist empId or empId exist in
		// department or position
		return false;
	}

	// select with empId, departmentIds, positionIds
	public List<ApprovalStep> findApprovalStepDetail(Integer employeeId, List<Position> positions,
			List<Department> departments) {
		StringBuilder hql = new StringBuilder();
		List<ApprovalStep> approvalSteps = new ArrayList<>();
		// Checl if employeeId cannot found => check employeeId in department and
		// position
//	    String listStatus = status.stream().map(stat -> "'" + stat + "'").collect(Collectors.joining(","));

		hql.append("from approval_steps appStep ");
		hql.append("inner join appStep.approvalStepDetails as appStepDetail ");
		hql.append("where appStepDetail.employeeId = :employeeId ");
		hql.append("or appStepDetail.positionId in (");
		if (positions != null && positions.size() > 0) {
			String listPosition = positions.stream().map(pos -> pos.getId() + "").collect(Collectors.joining(","));
			hql.append(listPosition);
		} else {
			hql.append("''");
		}

		hql.append(") ");
		hql.append("or appStepDetail.departmentId in (");
		if (departments != null && departments.size() > 0) {
			String listDepartment = departments.stream().map(dep -> dep.getId() + "").collect(Collectors.joining(","));
			hql.append(listDepartment);
		} else {
			hql.append("''");
		}

		hql.append(") ");
		LOGGER.info(hql.toString());
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("employeeId", employeeId + "");
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] objects = (Object[]) it.next();
				ApprovalStep approvalStep = (ApprovalStep) objects[0];
				approvalSteps.add(approvalStep);
			}

		} catch (Exception e) {
			LOGGER.error(e.toString());
			return null;
		}
		return approvalSteps;
	}

	public Set<Proposal> findAllApproveByMe(Integer proposalTypeId, List<Integer> statusIds,
			Integer creator, String createDateFrom, String createDateTo, String step, String sort, String order) {
		Set<Proposal> proposals = new LinkedHashSet();
		StringBuilder hql = new StringBuilder("FROM proposals AS pro ");
		hql.append("INNER JOIN pro.creator AS em ");
		hql.append("INNER JOIN pro.proposalType AS pt ");
		hql.append("INNER JOIN pro.status AS st ");
		hql.append("INNER JOIN pro.proposalDetails AS pd ");
		hql.append("WHERE pt.id = :proposalTypeId ");
		hql.append("AND pro.currentStep >= :step ");
//		hql.append("WHERE pd.fieldId = 1 ");
//		hql.append("AND pt.name LIKE CONCAT('%',:proposalType,'%') ");
		hql.append("AND st.id IN (:statusIds) ");
//		hql.append("AND em.name LIKE CONCAT('%',:creator,'%') ");
//		hql.append("AND pd.content LIKE CONCAT('%',:content,'%') ");
//		hql.append("AND pro.createDate LIKE CONCAT('%',:createDate,'%') ");
//		hql.append("AND pt.id LIKE CONCAT('%',:proposalTypeId,'%') ");
		if (creator != null && creator!=0) {
			hql.append("AND em.id = :creator ");
		}
		if (createDateFrom != null&& createDateTo == null ) {
			hql.append("AND pro.createDate = :createDateFrom ");
		}
		if (createDateFrom != null  && createDateTo != null) {
			hql.append("AND pro.createDate BETWEEN :createDateFrom AND :createDateTo ");
		}
		hql.append("ORDER BY " + sort + " " + order);
		try {
			Session session = sessionFactory.getCurrentSession();
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			query.setParameter("proposalTypeId", proposalTypeId);
			query.setParameter("statusIds", statusIds);
//			query.setParameter("creator", creator);
//			query.setParameter("content", content);
//			query.setParameter("createDate", createDate);
//			query.setParameter("proposalTypeId", proposalTypeId);
			query.setParameter("step", step);
			if (creator != null && creator!=0) {
				query.setParameter("creator", creator);
			}
			if ((createDateFrom != null && !createDateFrom.equals(""))&& (createDateTo == null || createDateTo.equals(""))) {
				query.setParameter("createDateFrom", createDateFrom);

			}
			if ((createDateFrom != null && !createDateFrom.equals("")) && (createDateTo != null && !createDateTo.equals(""))) {
				query.setParameter("createDateFrom", createDateFrom);
				query.setParameter("createDateTo", createDateTo);

			}
//			query.setFirstResult(offset);
//			query.setMaxResults(limit);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] objects = (Object[]) it.next();
				Proposal proposalTemp = (Proposal) objects[0];
				proposals.add(proposalTemp);
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		return proposals;
	}
	public Set<Proposal> findAllForAll(Integer  proposalTypeId, List<Integer> statusIds,
			Integer creator, String createDateFrom, String createDateTo, String sort, String order){
		Set<Proposal> proposals = new LinkedHashSet();
		StringBuilder hql = new StringBuilder("FROM proposals AS pro ");
		hql.append("INNER JOIN pro.creator AS em ");
		hql.append("INNER JOIN pro.proposalType AS pt ");
		hql.append("INNER JOIN pro.status AS st ");
		hql.append("INNER JOIN pro.proposalDetails AS pd ");
		
//		hql.append("WHERE pd.fieldId = 1 ");
//		hql.append("AND pt.name LIKE CONCAT('%',:proposalType,'%') ");
		hql.append("WHERE st.id IN (:statusIds) ");
//		hql.append("AND em.name LIKE CONCAT('%',:creator,'%') ");
//		hql.append("AND pd.content LIKE CONCAT('%',:content,'%') ");
//		hql.append("AND pro.createDate LIKE CONCAT('%',:createDate,'%') ");
//		hql.append("AND pt.id LIKE CONCAT('%',:proposalTypeId,'%') ");
		if(proposalTypeId!=null) {
			hql.append("AND pt.id = :proposalTypeId ");
		}
		if (creator != null) {
			hql.append("AND em.id = :creator ");
		}
		if (createDateFrom != null && createDateTo == null) {
			hql.append("AND pro.createDate = :createDateFrom ");
		}
		if (createDateFrom != null && createDateTo != null) {
			hql.append("AND pro.createDate BETWEEN :createDateFrom AND :createDateTo ");
		}
		hql.append("ORDER BY " + sort + " " + order);
		try {
			Session session = sessionFactory.getCurrentSession();
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			if(proposalTypeId!=null) {
				query.setParameter("proposalTypeId", proposalTypeId);
			}
			query.setParameter("statusIds", statusIds);
			if (creator != null ) {
				query.setParameter("creator", creator);
			}
			if (createDateFrom != null && createDateTo == null) {
				query.setParameter("createDateFrom", createDateFrom);
			}
			if (createDateFrom != null && createDateTo != null) {
				query.setParameter("createDateFrom", createDateFrom);
				query.setParameter("createDateTo", createDateTo);

			}
//			query.setFirstResult(offset);
//			query.setMaxResults(limit);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] objects = (Object[]) it.next();
				Proposal proposalTemp = (Proposal) objects[0];
				proposals.add(proposalTemp);
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		return proposals;
	}
	public Set<Proposal> findAllCreatedByMe(Integer employeeId, Integer  proposalTypeId, List<Integer> statusIds,
			Integer creator, String createDateFrom, String createDateTo, String sort, String order){
		Set<Proposal> proposals = new LinkedHashSet();
		StringBuilder hql = new StringBuilder("FROM proposals AS pro ");
		hql.append("INNER JOIN pro.creator AS em ");
		hql.append("INNER JOIN pro.proposalType AS pt ");
		hql.append("INNER JOIN pro.status AS st ");
		hql.append("INNER JOIN pro.proposalDetails AS pd ");
		
//		hql.append("WHERE pd.fieldId = 1 ");
//		hql.append("AND pt.name LIKE CONCAT('%',:proposalType,'%') ");
		hql.append("WHERE st.id IN (:statusIds) ");
//		hql.append("AND em.name LIKE CONCAT('%',:creator,'%') ");
//		hql.append("AND pd.content LIKE CONCAT('%',:content,'%') ");
//		hql.append("AND pro.createDate LIKE CONCAT('%',:createDate,'%') ");
//		hql.append("AND pt.id LIKE CONCAT('%',:proposalTypeId,'%') ");
		if(proposalTypeId!=null) {
			hql.append("AND pt.id = :proposalTypeId ");
		}
		if (creator != null) {
			hql.append("AND em.id = :creator ");
		}else {
			hql.append("AND em.id = :employeeId ");
		}
		if (createDateFrom != null && createDateTo == null) {
			hql.append("AND pro.createDate = :createDateFrom ");
		}
		if (createDateFrom != null && createDateTo != null) {
			hql.append("AND pro.createDate BETWEEN :createDateFrom AND :createDateTo ");
		}
		hql.append("ORDER BY " + sort + " " + order);
		try {
			Session session = sessionFactory.getCurrentSession();
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			if(proposalTypeId!=null) {
				query.setParameter("proposalTypeId", proposalTypeId);
			}
			query.setParameter("statusIds", statusIds);
			if (creator != null ) {
				query.setParameter("creator", creator);
			}else {
				query.setParameter("employeeId", employeeId);
			}
			if (createDateFrom != null && createDateTo == null) {
				query.setParameter("createDateFrom", createDateFrom);
			}
			if (createDateFrom != null && createDateTo != null) {
				query.setParameter("createDateFrom", createDateFrom);
				query.setParameter("createDateTo", createDateTo);

			}
//			query.setFirstResult(offset);
//			query.setMaxResults(limit);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] objects = (Object[]) it.next();
				Proposal proposalTemp = (Proposal) objects[0];
				proposals.add(proposalTemp);
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		return proposals;
	}
	@Override
	public Integer countAllPaging(Integer employeeId, Integer proposal, String content, String status, String creator,
			String createDate, String finishDate, String sort, String order, Integer offset, Integer limit) {
		List<ProposalModel> proposalModels = new ArrayList<>();
		StringBuilder hql = new StringBuilder("FROM proposals AS pro ");
		hql.append("INNER JOIN pro.employee AS em ");
		hql.append("INNER JOIN pro.proposalType AS pt ");
		hql.append("INNER JOIN pro.status AS st ");
		hql.append("INNER JOIN pro.proposalDetails AS pd ");
		hql.append("WHERE pd.proposalDetailIndex = 1 ");
		hql.append("AND pt.name LIKE CONCAT('%',:proposal,'%') ");
		hql.append("AND st.name LIKE CONCAT('%',:status,'%') ");
		hql.append("AND em.name LIKE CONCAT('%',:creator,'%') ");
		hql.append("AND pd.content LIKE CONCAT('%',:content,'%') ");
		hql.append("AND pro.createDate LIKE CONCAT('%',:createDate,'%') ");
		hql.append("ORDER BY " + sort + " " + order);
		int count = -1;
		try {
			Session session = sessionFactory.getCurrentSession();
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			query.setParameter("proposal", proposal);
			query.setParameter("status", status);
			query.setParameter("creator", creator);
			query.setParameter("content", content);
			query.setParameter("createDate", createDate);
			query.setFirstResult(offset);
			query.setMaxResults(limit);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] objects = (Object[]) it.next();
				ProposalModel proposalModel = new ProposalModel();
				proposalModels.add(proposalModel);

			}
			count = proposalModels != null ? proposalModels.size() : 0;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return count;
	}

	@Override
	public ProposalModel findById(Integer id) {
		ProposalModel proposalModel = new ProposalModel();
		try {
			Session session = sessionFactory.getCurrentSession();
			/*
			 * select * from proposals AS pro INNER JOIN statuses AS stu ON pro.status_id =
			 * stu.id INNER JOIN employees AS emp ON pro.creator_id = emp.id INNER JOIN
			 * proposal_types AS prt ON pro.proposal_type_id = prt.id INNER JOIN
			 * proposal_details AS prd ON pro.id = prd.proposal_id WHERE pro.id = 1;
			 */
			StringBuilder hql = new StringBuilder();
			hql.append("FROM proposals AS pro ");
			hql.append("INNER JOIN statuses AS stu ON pro.status.id = stu.id ");
			hql.append("INNER JOIN employees AS emp ON pro.creator.id = emp.id ");
			hql.append("INNER JOIN employees AS emp ON pro.receiver.id = emp.id ");
			hql.append("INNER JOIN proposal_types AS prt ON pro.proposalType.id = prt.id ");
			hql.append("INNER JOIN proposal_details AS prd ON pro.id = prd.proposalId.id ");
			hql.append("WHERE pro.id = :id");
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);

			List<ContentModel> contents = new ArrayList<ContentModel>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Proposal proposal = (Proposal) obj[0];
				Status status = (Status) obj[1];
				Employee e = (Employee) obj[2];
				Employee r = (Employee) obj[3];
				ProposalType proposalType = (ProposalType) obj[4];
				ProposalDetail proposalDetail = (ProposalDetail) obj[5];

				if (0 == proposalModel.getId()) {
					proposalModel.setId(proposal.getId());
				}
				if (null == proposalModel.getProposal()) {
					proposalModel.setProposal(proposalType);
				}
				if (null == proposalModel.getStatus()) {
					proposalModel.setStatus(status);
				}

				if (null == proposalModel.getCreator()) {
					EmployeeModel employeeModel = new EmployeeModel();
					employeeModel.setId(e.getId());
					employeeModel.setCode(e.getCode());
					employeeModel.setName(e.getName());
					employeeModel.setAvatar(e.getAvatar());
					employeeModel.setGender(e.getGender());
					employeeModel.setDateOfBirth(e.getDateOfBirth());
					employeeModel.setEmail(e.getEmail());
					employeeModel.setPhoneNumber(e.getPhoneNumber());
					employeeModel.setActive(e.isActive());
					employeeModel.setCreateDate(e.getCreateDate());

					List<DepartmentModel> departmentModels = new ArrayList<DepartmentModel>();
					for (Department department : e.getDepartments()) {
						DepartmentModel dModel = new DepartmentModel();
						dModel.setCode(department.getCode());
						dModel.setDescription(department.getDescription());
						dModel.setId(department.getId());
						dModel.setName(department.getName());
						dModel.setLevel(department.getLevel());
						departmentModels.add(dModel);
					}
					employeeModel.setDepartments(departmentModels);
					;

					List<PositionModel> positionModels = new ArrayList<PositionModel>();
					for (Position po : e.getPositions()) {
						PositionModel positionModel = new PositionModel();
						positionModel.setId(po.getId());
						positionModel.setName(po.getName());
						positionModel.setRole(po.getRole());
						positionModel.setIsManager(po.getIsManager());
						positionModels.add(positionModel);
					}
					employeeModel.setPositions(positionModels);
					employeeModel.setCreateDate(e.getCreateDate());
					employeeModel.setModifyDate(e.getModifyDate());
					employeeModel.setCreateBy(e.getCreateBy());
					employeeModel.setModifyBy(e.getModifyBy());
					proposalModel.setCreator(employeeModel);
				}

				if (null == proposalModel.getCreatedDate()) {
					proposalModel.setCreatedDate(proposal.getCreateDate());
				}

				ContentModel contentModel = new ContentModel();
				contentModel.setFieldId(proposalDetail.getFieldId());
				contentModel.setContent(proposalDetail.getContent());
				contentModel.setFieldName(proposalDetail.getFieldName());
				contents.add(contentModel);
			}
			proposalModel.setContents(contents);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return proposalModel;
	}

	@Override
	public ProposalModel add(Proposal proposal, List<ProposalDetail> proposalDetails) {
		ProposalModel proposalModel = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer resultAddProposal = (Integer) session.save(proposal);
			if (resultAddProposal == -1) {
				return proposalModel;
			}

			for (ProposalDetail proposalDetail : proposalDetails) {
				proposal.setId(resultAddProposal);
				proposalDetail.setProposalId(proposal);
				Integer resultAddProposalDetail = (Integer) session.save(proposalDetail);
				if (resultAddProposalDetail == -1) {
					return proposalModel;
				}
			}
			LOGGER.info("Add proposal successfully");
			proposalModel = new ProposalModel();
			proposalModel.setId(resultAddProposal);

			EmployeeModel creator = new EmployeeModel();
			creator.setId(proposal.getCreator().getId());
			creator.setName(proposal.getCreator().getName());
			creator.setCode(proposal.getCreator().getCode());
			creator.setAvatar(proposal.getCreator().getAvatar());
			creator.setGender(proposal.getCreator().getGender());
			creator.setDateOfBirth(proposal.getCreator().getDateOfBirth());
			creator.setEmail(proposal.getCreator().getEmail());
			creator.setPhoneNumber(proposal.getCreator().getPhoneNumber());
			creator.setActive(proposal.getCreator().isActive());
			creator.setCreateDate(proposal.getCreator().getCreateDate());
			creator.setCreateDate(proposal.getCreator().getCreateDate());
			creator.setModifyDate(proposal.getCreator().getModifyDate());
			creator.setCreateBy(proposal.getCreator().getCreateBy());
			creator.setModifyBy(proposal.getCreator().getModifyBy());
			proposalModel.setCreator(creator);

			EmployeeModel receiver = new EmployeeModel();
			receiver.setId(proposal.getReceiver().getId());
			receiver.setName(proposal.getReceiver().getName());
			receiver.setCode(proposal.getReceiver().getCode());
			receiver.setAvatar(proposal.getReceiver().getAvatar());
			receiver.setGender(proposal.getReceiver().getGender());
			receiver.setDateOfBirth(proposal.getReceiver().getDateOfBirth());
			receiver.setEmail(proposal.getReceiver().getEmail());
			receiver.setPhoneNumber(proposal.getReceiver().getPhoneNumber());
			receiver.setActive(proposal.getReceiver().isActive());
			receiver.setCreateDate(proposal.getReceiver().getCreateDate());
			receiver.setCreateDate(proposal.getReceiver().getCreateDate());
			receiver.setModifyDate(proposal.getReceiver().getModifyDate());
			receiver.setCreateBy(proposal.getReceiver().getCreateBy());
			receiver.setModifyBy(proposal.getReceiver().getModifyBy());
			proposalModel.setReceiver(receiver);
			proposalModel.setProposal(proposal.getProposalType());

			List<ContentModel> contentModels = new ArrayList<ContentModel>();
			for (ProposalDetail proposalDetail : proposalDetails) {
				ContentModel contentModel = new ContentModel();
				contentModel.setFieldId(proposalDetail.getFieldId());
				contentModel.setFieldName(proposalDetail.getFieldName());
				contentModel.setContent(proposalDetail.getContent());
				contentModels.add(contentModel);
			}
			proposalModel.setContents(contentModels);
			proposalModel.setCreatedDate(proposal.getCreateDate());
			proposalModel.setStatus(proposal.getStatus());

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return proposalModel;
	}

}
