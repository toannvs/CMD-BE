package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.ApprovalStep;
import com.comaymanagement.cmd.entity.ApprovalStepDetail;
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
import com.comaymanagement.cmd.service.APIService;
import com.comaymanagement.cmd.service.UserDetailsImpl;

@Repository
@Transactional(rollbackFor = Exception.class)
public class ProposalRepositoryImpl implements IProposalRepository {

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	DepartmentRepositoryImpl departmentRepository;

	@Autowired
	PositionRepositoryImpl positionRepository;
	@Autowired
	ApprovalStepRepositoryImpl approvalStepRepository;

	@Autowired
	NotifyRepositoryImpl notifyRepositoryImpl;

	@Autowired
	ApprovalStepDetailRepositoryImpl approvalStepDetailRepository;
	@Autowired
	EmployeeRepositoryImpl employeeRepositoryImpl;
	
	private final Logger LOGGER = LoggerFactory.getLogger(ProposalRepositoryImpl.class);
	public int countAllForAll = 0;
	public int countAllForProposalApproveByMe = 0;
	public int countAllForProposalCratedByMe = 0;
	// proposals

	public List<ProposalModel> findAllProposalForAll(List<Integer> proposalTypeIds, List<Integer> statusIds,
			List<Integer> creatorIds, String createDateFrom, String createDateTo, String sort, String order,
			Integer offset, Integer limit) {
		List<Proposal> proposals = new ArrayList<>();
		List<ProposalModel> proposalModelResult = new ArrayList<>();
		Set<Proposal> proposalsTMP = new LinkedHashSet();
		proposalsTMP = findAllForAll(proposalTypeIds, statusIds, creatorIds, createDateFrom, createDateTo, sort, order);
		// store proposal of each proposalType and step
		if (proposalsTMP != null && proposalsTMP.size() > 0) {
			for (Proposal pro : proposalsTMP) {
				proposals.add(pro);
			}
		}
		// set value fo total
		setCountAllForAll(proposals.size());
		if(offset !=-1 && limit !=-1) {
			// paging
			for (int i = offset; i < proposals.size() && proposalModelResult.size() < limit; i++) {
				ProposalModel proposalModel = this.findModelById(proposals.get(i).getId());
				proposalModelResult.add(proposalModel);
			}
		}else {
			for (Proposal pro : proposals) {
				ProposalModel proposalModel = this.findModelById(pro.getId());
				proposalModelResult.add(proposalModel);
			}
		}
		
		return proposalModelResult;
	}

	@Override
	public List<ProposalModel> findAllProposalApproveByMe(Integer employeeId, List<Integer> proposalTypeIds,
			List<Integer> statusIds, List<Integer> creatorIds, String createDateFrom, String createDateTo, String sort,
			String order, Integer offset, Integer limit) {
		List<ApprovalStep> appSteps = new ArrayList<>();
		List<Proposal> proposals = new ArrayList<>();
		List<ProposalModel> proposalModelResult = new ArrayList<>();
		List<Integer> positionIds = new ArrayList<>();
		List<Integer> departmentIds = new ArrayList<>();
		List<Position> positionTMPs = positionRepository.findAllByEmployeeId(employeeId);
		List<Department> departmentTMPs = departmentRepository.findAllByEmployeeId(employeeId);
		for (Department d : departmentTMPs) {
			departmentIds.add(d.getId());
		}
		for (Position p : positionTMPs) {
			positionIds.add(p.getId());
		}

		appSteps = findApprovalStepDetail(employeeId, positionIds, departmentIds);

		for (ApprovalStep appStep : appSteps) {
			// Check if fillter with proposal type id
			if (proposalTypeIds.size() > 0) {
				for (Integer proposalTypeId : proposalTypeIds) {
					if (proposalTypeId == appStep.getProposalType().getId()) {
						Integer step = appStep.getApprovalStepIndex();
						Set<Proposal> proposalsTMP = new LinkedHashSet();
						proposalsTMP = findAllApproveByMe(proposalTypeId, statusIds, creatorIds, createDateFrom,
								createDateTo, step, sort, order);
						// store proposal of each proposalType and step
						if (proposalsTMP != null && proposalsTMP.size() > 0) {
							for (Proposal pro : proposalsTMP) {
								proposals.add(pro);
							}
						}
					}
				}
			} else {
				Integer currentEmpProposalTypeId = appStep.getProposalType().getId();
				Integer step = appStep.getApprovalStepIndex();
				Set<Proposal> proposalsTMP = new LinkedHashSet();
				proposalsTMP = findAllApproveByMe(currentEmpProposalTypeId, statusIds, creatorIds, createDateFrom,
						createDateTo, step, sort, order);
				// store proposal of each proposalType and step
				if (proposalsTMP != null && proposalsTMP.size() > 0) {
					for (Proposal pro : proposalsTMP) {
						proposals.add(pro);
					}
				}
			}

		}
		// set value fo total
		setCountAllForProposalApproveByMe(proposals.size());
		if(offset !=-1 && limit !=-1) {
		// paging
			for (int i = offset; i < proposals.size() && proposalModelResult.size() < limit; i++) {
				ProposalModel proposalModel = this.findModelById(proposals.get(i).getId());
				proposalModelResult.add(proposalModel);
			}
		}else {
			for (Proposal pro : proposals) {
				ProposalModel proposalModel = this.findModelById(pro.getId());
				proposalModelResult.add(proposalModel);
			}
		}
		return proposalModelResult;
	}

	public List<ProposalModel> findAllProposalCratedByMe(Integer employeeId, List<Integer> proposalTypeIds,
			List<Integer> statusIds, String createDateFrom, String createDateTo, String sort, String order,
			Integer offset, Integer limit) {
		List<Proposal> proposals = new ArrayList<>();
		List<ProposalModel> proposalModelResult = new ArrayList<>();
		Set<Proposal> proposalsTMP = new LinkedHashSet();
		proposalsTMP = findAllCreatedByMe(employeeId, proposalTypeIds, statusIds, createDateFrom, createDateTo, sort,
				order);
		// store proposal of each proposalType and step
		if (proposalsTMP != null && proposalsTMP.size() > 0) {
			for (Proposal pro : proposalsTMP) {
				proposals.add(pro);
			}
		}
		// set value fo total
		setCountAllForProposalCratedByMe(proposals.size());
		if(offset !=-1 && limit !=-1) {
			// paging
			for (int i = offset; i < proposals.size() && proposalModelResult.size() < limit; i++) {
				ProposalModel proposalModel = this.findModelById(proposals.get(i).getId());
				proposalModelResult.add(proposalModel);
			}
		}else {
			for (Proposal pro : proposals) {
				ProposalModel proposalModel = this.findModelById(pro.getId());
				proposalModelResult.add(proposalModel);
			}
		}
		return proposalModelResult;
	}

	public boolean checkExitsInStep(String employeeId) {
		// Check all record of approvalStepDetail is exist empId or empId exist in
		// department or position
		return false;
	}

	// select with empId, departmentIds, positionIds
	public List<ApprovalStep> findApprovalStepDetail(Integer employeeId, List<Integer> positions,
			List<Integer> departments) {
		StringBuilder hql = new StringBuilder();
		List<ApprovalStep> approvalSteps = new ArrayList<>();
		// Checl if employeeId cannot found => check employeeId in department and
		// position
//	    String listStatus = status.stream().map(stat -> "'" + stat + "'").collect(Collectors.joining(","));

		hql.append("from approval_steps appStep ");
		hql.append("inner join appStep.approvalStepDetails as appStepDetail ");
		hql.append("where appStepDetail.employeeId = :employeeId ");
		if (positions != null && positions.size() > 0) {
			hql.append("or appStepDetail.positionId IN (:positions) ");
		}

		if (departments != null && departments.size() > 0) {
			hql.append("or appStepDetail.departmentId IN (:departments) ");
		}
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
				ApprovalStep approvalStep = (ApprovalStep) objects[0];
				approvalSteps.add(approvalStep);
			}

		} catch (Exception e) {
			LOGGER.error(e.toString());
			e.printStackTrace();
			return null;
		}
		return approvalSteps;
	}

	public Set<Proposal> findAllApproveByMe(Integer proposalTypeId, List<Integer> statusIds, List<Integer> creatorIds,
			String createDateFrom, String createDateTo, Integer step, String sort, String order) {
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
		if (creatorIds.size() > 0) {
			hql.append("AND em.id IN (:creatorIds) ");
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
			query.setParameter("proposalTypeId", proposalTypeId);
			query.setParameter("statusIds", statusIds);
//			query.setParameter("creator", creator);
//			query.setParameter("content", content);
//			query.setParameter("createDate", createDate);
//			query.setParameter("proposalTypeId", proposalTypeId);
			query.setParameter("step", step);
			if (creatorIds.size() > 0) {
				query.setParameter("creatorIds", creatorIds);
			}
			if ((createDateFrom != null && !createDateFrom.equals(""))
					&& (createDateTo == null || createDateTo.equals(""))) {
				query.setParameter("createDateFrom", createDateFrom);

			}
			if ((createDateFrom != null && !createDateFrom.equals(""))
					&& (createDateTo != null && !createDateTo.equals(""))) {
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

	public Set<Proposal> findAllForAll(List<Integer> proposalTypeIds, List<Integer> statusIds, List<Integer> creatorIds,
			String createDateFrom, String createDateTo, String sort, String order) {
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
		if (proposalTypeIds != null && proposalTypeIds.size() > 0) {
			hql.append("AND pt.id IN (:proposalTypeIds) ");
		}
		if (creatorIds != null && creatorIds.size() > 0) {
			hql.append("AND em.id IN (:creatorIds) ");
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
			if (proposalTypeIds.size() > 0) {
				query.setParameter("proposalTypeIds", proposalTypeIds);
			}
			query.setParameter("statusIds", statusIds);
			if (creatorIds.size() > 0) {
				query.setParameter("creatorIds", creatorIds);
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

	public Set<Proposal> findAllCreatedByMe(Integer employeeId, List<Integer> proposalTypeIds, List<Integer> statusIds,
			String createDateFrom, String createDateTo, String sort, String order) {
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
		hql.append("AND em.id = :employeeId ");
		if (proposalTypeIds != null && proposalTypeIds.size() > 0) {
			hql.append("AND pt.id IN (:proposalTypeIds) ");
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
			if (proposalTypeIds != null && proposalTypeIds.size() > 0) {
				query.setParameter("proposalTypeIds", proposalTypeIds);
			}
			query.setParameter("statusIds", statusIds);
			query.setParameter("employeeId", employeeId);
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
	public ProposalModel findModelById(Integer id) {
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
			hql.append("INNER JOIN proposal_types AS prt ON pro.proposalType.id = prt.id ");
			hql.append("INNER JOIN proposal_details AS prd ON pro.id = prd.proposalId.id ");
			hql.append("WHERE pro.id = :id");
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
			Proposal proposal = new Proposal();
			List<ContentModel> contents = new ArrayList<ContentModel>();
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				proposal = (Proposal) obj[0];
				Status status = (Status) obj[1];
				Employee e = (Employee) obj[2];
				ProposalType proposalType = (ProposalType) obj[3];
				ProposalDetail proposalDetail = (ProposalDetail) obj[4];

				if (0 == proposalModel.getId()) {
					proposalModel.setId(proposal.getId());
				}
				if (null == proposalModel.getProposalType()) {
					proposalModel.setProposalType(proposalType);
				}
				if (null == proposalModel.getStatus()) {
					proposalModel.setStatus(status);
				}

				if (null == proposalModel.getCreator()) {
					EmployeeModel employeeModel = new EmployeeModel();
					employeeModel.setId(e.getId());
					employeeModel.setCode(e.getCode());
					employeeModel.setName(e.getName());
					employeeModel.setAvatar(APIService.convertToBase64(e.getAvatar()));
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
				contentModel.setId(proposalDetail.getId());
				contentModel.setFieldId(proposalDetail.getFieldId());
				contentModel.setContent(proposalDetail.getContent());
				contentModel.setFieldName(proposalDetail.getFieldName());
				contents.add(contentModel);
			}
			proposalModel.setCurrentStep(proposal.getCurrentStep());
			proposalModel.setContents(contents);
			proposalModel.setReason(proposal.getReason());
			proposalModel.setCanApprove(checkIfCanApprove(proposalModel.getProposalType().getId(), proposalModel.getCurrentStep().toString()));
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
			creator.setAvatar(APIService.convertToBase64(proposal.getCreator().getAvatar()));
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

			proposalModel.setProposalType(proposal.getProposalType());

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
			proposalModel.setCurrentStep(proposal.getCurrentStep());
			proposalModel.setCanApprove(checkIfCanApprove(proposalModel.getProposalType().getId(), proposalModel.getCurrentStep().toString()));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return proposalModel;
	}

	public Proposal findById(Integer id) {
		StringBuilder hql = new StringBuilder();
		hql.append("FROM proposals AS pro ");
		hql.append("WHERE pro.id = :id");
		Proposal proposal = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			LOGGER.info(hql.toString());
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
				proposal = (Proposal) query.getSingleResult();
			return proposal;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
	public ProposalModel edit(Proposal proposal, List<ProposalDetail> proposalDetails) {
		ProposalModel proposalModel = new ProposalModel();
		Integer resultEditedProposal = -1;
		Integer resultAddProposalDetail = -1;
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(proposal);
			resultEditedProposal = 1;
			if (resultEditedProposal < 0 ) {
				return null;
			}
			if(proposalDetails!=null) {
				for (ProposalDetail proposalDetail : proposalDetails) {
					proposalDetail.setProposalId(proposal);
					session.update(proposalDetail);
					resultAddProposalDetail = 1;
					if (resultAddProposalDetail < 0) {
						return null;
					}
				}
			}
			// response data
			proposalModel = new ProposalModel();
			proposalModel.setId(proposal.getId());
			EmployeeModel creator = new EmployeeModel();
			creator.setId(proposal.getCreator().getId());
			creator.setName(proposal.getCreator().getName());
			creator.setCode(proposal.getCreator().getCode());
			creator.setAvatar(APIService.convertToBase64(proposal.getCreator().getAvatar()));
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

			proposalModel.setProposalType(proposal.getProposalType());

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
			proposalModel.setCurrentStep(proposal.getCurrentStep());
			proposalModel.setCanApprove(checkIfCanApprove(proposalModel.getProposalType().getId(), proposalModel.getCurrentStep().toString()));
			return proposalModel;
		} catch (Exception e) {
			LOGGER.error("Error has occured in edit() ", e);
			return null;
		}
	}

	public int getCountAllForAll() {
		return countAllForAll;
	}

	public void setCountAllForAll(int countAllForAll) {
		this.countAllForAll = countAllForAll;
	}

	public int getCountAllForProposalApproveByMe() {
		return countAllForProposalApproveByMe;
	}

	public void setCountAllForProposalApproveByMe(int countAllForProposalApproveByMe) {
		this.countAllForProposalApproveByMe = countAllForProposalApproveByMe;
	}

	public int getCountAllForProposalCratedByMe() {
		return countAllForProposalCratedByMe;
	}

	public void setCountAllForProposalCratedByMe(int countAllForProposalCratedByMe) {
		this.countAllForProposalCratedByMe = countAllForProposalCratedByMe;
	}
	public boolean checkIfCanApprove(Integer proposalTypeId, String currentStep) {
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		List<ApprovalStep> approvalStep = approvalStepRepository.findByProposalTypeIdAndIndexForCheck(proposalTypeId, currentStep);
		List<Integer> employeeIds = new ArrayList<>();
		List<ApprovalStepDetail> approvalStepDetails = new ArrayList<>();
		for(ApprovalStep appStep : approvalStep) {
			// One app step have many appStepDetail
			approvalStepDetails = approvalStepDetailRepository.findAllByApprovalStepId(appStep.getId());
			for(ApprovalStepDetail appStepDetail : approvalStepDetails) {
				// One appStepDetail have many record;
				employeeIds.add(appStepDetail.getEmployeeId());
				for(Employee emp : employeeRepositoryImpl.findByPositionId(appStepDetail.getPositionId())) {
					employeeIds.add(emp.getId());
				}
				for(Employee emp : employeeRepositoryImpl.findByDepartmentId(appStepDetail.getDepartmentId())) {
					employeeIds.add(emp.getId());
				}
				
			}
		}
		if(employeeIds.contains(userDetail.getId())) {
			return true;
		}
		return false;
	}
}
