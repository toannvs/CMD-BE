package com.comaymanagement.cmd.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.constant.CMDConstrant;
import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.ApprovalStep;
import com.comaymanagement.cmd.entity.ApprovalStepDetail;
import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Notify;
import com.comaymanagement.cmd.entity.Pagination;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.Proposal;
import com.comaymanagement.cmd.entity.ProposalDetail;
import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Status;
import com.comaymanagement.cmd.model.NotifyModel;
import com.comaymanagement.cmd.model.ProposalModel;
import com.comaymanagement.cmd.model.StatusModel;
import com.comaymanagement.cmd.repositoryimpl.ApprovalStepDetailRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ApprovalStepRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.DepartmentRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.NotifyRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.PositionRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalTypeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.StatusRepositotyImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
public class ProposalService {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	Message message;

	@Autowired
	ProposalRepositoryImpl proposalRepositoryImpl;

	@Autowired
	ProposalTypeRepositoryImpl proposalTypeRepositoryImpl;

	@Autowired
	EmployeeRepositoryImpl employeeRepositoryImpl;

	@Autowired
	StatusRepositotyImpl statusRepositotyImpl;

	@Autowired
	ApprovalStepRepositoryImpl approvalStepRepository;

	@Autowired
	NotifyRepositoryImpl notifyRepositoryImpl;

	@Autowired
	PositionRepositoryImpl posi;
	
	@Autowired
	PositionRepositoryImpl positionRepository;
	
	@Autowired
	DepartmentRepositoryImpl departmentRepository;

	public ResponseEntity<Object> findAllForAll(String json, String sort, String order, String page) {
		List<ProposalModel> proposalModels = new ArrayList<>();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String createDateFrom = null;
		String createDateTo = null;
		List<Integer> creatorIds = new ArrayList<Integer>();
		List<Integer> proposalTypeIds = new ArrayList<>();
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Map<String, Object> results = new TreeMap<String, Object>();
		try {
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			JsonNode jsonCreatorIds = jsonObject.get("creatorIds");
			JsonNode jsonProposalTypeIds = jsonObject.get("proposalTypeIds");

			for (JsonNode creatorId : jsonCreatorIds) {
				creatorIds.add(Integer.valueOf(creatorId.toString()));
			}
			for (JsonNode proposalTypeId : jsonProposalTypeIds) {
				proposalTypeIds.add(Integer.valueOf(proposalTypeId.toString()));
			}
			createDateFrom = (jsonObject.get("createDateFrom") != null
					&& !jsonObject.get("createDateFrom").asText().equals("null")
					&& !jsonObject.get("createDateFrom").asText().equals(""))
							? jsonObject.get("createDateFrom").asText()
							: null;
			createDateTo = (jsonObject.get("createDateTo") != null
					&& !jsonObject.get("createDateTo").asText().equals("null")
					&& !jsonObject.get("createDateTo").asText().equals("")) ? jsonObject.get("createDateTo").asText()
							: null;
			page = page == null ? "1" : page.trim();
			int limit = CMDConstrant.LIMIT;
			int offset = (Integer.parseInt(page) - 1) * limit;
			List<Integer> statusIds = new ArrayList<Integer>();
			for (JsonNode statusId : jsonStatusObject) {
				statusIds.add(Integer.valueOf(statusId.toString()));
			}
			if (statusIds.size() == 0) {
				List<Status> statuses = statusRepositotyImpl.findAllForProposal();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
			}
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "pro.createDate";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			proposalModels = proposalRepositoryImpl.findAllProposalForAll(proposalTypeIds, statusIds, creatorIds,
					createDateFrom, createDateTo, sort, order, offset, limit);


			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(proposalRepositoryImpl.getCountAllForAll());
			results.put("pagination", pagination);
			results.put("proposals", proposalModels);

			if (results.size() > 0) {
				// Count by status
				List<Status> statuses = statusRepositotyImpl.findAllForProposal();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
				Map<Integer, Integer> count = proposalRepositoryImpl.countStatusProposalForAll();
				List<StatusModel> statusModels = new ArrayList<>();
				for (Status status : statuses) {
					Integer amount = count.get(status.getId());
					StatusModel statusModel = new StatusModel();
					statusModel.setId(status.getId());
					statusModel.setName(status.getName());
					if(amount!=null) {
						statusModel.setCountByStatus(amount);
					}else {
						statusModel.setCountByStatus(0);
					}
					statusModels.add(statusModel);
				}
				results.put("countByStatuses", statusModels);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), results));
		}
	}

	public ResponseEntity<Object> findAllApproveByMe(String json, String sort, String order, String page) {
		List<ProposalModel> proposalModels = new ArrayList<>();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String createDateFrom = null;
		String createDateTo = null;
		List<Integer> creatorIds = new ArrayList<Integer>();
		List<Integer> proposalTypeIds = new ArrayList<>();
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		Map<String, Object> results = new TreeMap<String, Object>();
		try {
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			JsonNode jsonCreatorIds = jsonObject.get("creatorIds");
			JsonNode jsonProposalTypeIds = jsonObject.get("proposalTypeIds");

			for (JsonNode creatorId : jsonCreatorIds) {
				creatorIds.add(Integer.valueOf(creatorId.toString()));
			}
			for (JsonNode proposalTypeId : jsonProposalTypeIds) {
				proposalTypeIds.add(Integer.valueOf(proposalTypeId.toString()));
			}
			createDateFrom = (jsonObject.get("createDateFrom") != null
					&& !jsonObject.get("createDateFrom").asText().equals("null")
					&& !jsonObject.get("createDateFrom").asText().equals(""))
							? jsonObject.get("createDateFrom").asText()
							: null;
			createDateTo = (jsonObject.get("createDateTo") != null
					&& !jsonObject.get("createDateTo").asText().equals("null")
					&& !jsonObject.get("createDateTo").asText().equals("")) ? jsonObject.get("createDateTo").asText()
							: null;
			page = page == null ? "1" : page.trim();
			int limit = CMDConstrant.LIMIT;
			int offset = (Integer.parseInt(page) - 1) * limit;
			List<Integer> statusIds = new ArrayList<Integer>();
			for (JsonNode statusId : jsonStatusObject) {
				System.out.println(statusId.toString());
				statusIds.add(Integer.valueOf(statusId.toString()));
			}
			if (statusIds.size() == 0) {
				List<Status> statuses = statusRepositotyImpl.findAllForProposal();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
			}
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "pro.createDate";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			List<ApprovalStep> appSteps = new ArrayList<>();
		
			List<Integer> positionIds = new ArrayList<>();
			List<Integer> departmentIds = new ArrayList<>();
			Integer employeeId = userDetail.getId();
			List<Position> positionTMPs = positionRepository.findAllByEmployeeId(employeeId);
			List<Department> departmentTMPs = departmentRepository.findAllByEmployeeId(employeeId);
			for (Department d : departmentTMPs) {
				departmentIds.add(d.getId());
			}
			for (Position p : positionTMPs) {
				positionIds.add(p.getId());
			}

			appSteps = proposalRepositoryImpl.findApprovalStepDetail(employeeId, positionIds, departmentIds);

			proposalModels = proposalRepositoryImpl.findAllProposalApproveByMe(appSteps, proposalTypeIds,
					statusIds, creatorIds, createDateFrom, createDateTo, sort, order, offset, limit);

//			Integer totalProposal  = 0;
//			totalProposal = proposalRepositoryImpl.countAllPaging(userDetail.getId(), proposal, content, status, creator, createDate, finishDate, sort, order, offset, limit);
//			
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(proposalRepositoryImpl.getCountAllForProposalApproveByMe());

			results.put("pagination", pagination);
			results.put("proposals", proposalModels);

			if (results.size() > 0) {
				// Count by status
				List<Status> statuses = statusRepositotyImpl.findAllForProposal();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}				
//				List<ProposalModel> proposalModelForCount = proposalRepositoryImpl.findAllProposalApproveByMe(userDetail.getId(), new ArrayList<Integer>(),
//						statusIds, new ArrayList<Integer>(), null, null, sort, order, -1, -1);
				Map<Integer, Integer> finalResult = new LinkedHashMap<>();
				for(ApprovalStep appStep : appSteps) {
					Map<Integer, Integer> count = proposalRepositoryImpl.countStatusProposalApproveByMe(appStep.getProposalType().getId(), appStep.getApprovalStepIndex());
					if(count.size()>0) {
						for (Map.Entry<Integer, Integer> entry : count.entrySet()) {
							Integer valueCheckIfExist = finalResult.get(entry.getKey());
							if(valueCheckIfExist!=null) {
								finalResult.put(entry.getKey(),valueCheckIfExist +  entry.getValue());
							}else {
								finalResult.put(entry.getKey(),entry.getValue());
							}
						}
					}
				}
				List<StatusModel> statusModels = new ArrayList<>();
				for (Status status : statuses) {
					Integer amount = finalResult.get(status.getId());
					StatusModel statusModel = new StatusModel();
					statusModel.setId(status.getId());
					statusModel.setName(status.getName());
					if(amount!=null) {
						statusModel.setCountByStatus(amount);
					}else {
						statusModel.setCountByStatus(0);
					}
					statusModels.add(statusModel);
				}
				results.put("countByStatuses", statusModels);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), results));
		}
	}

	public ResponseEntity<Object> findAllCreatedByMe(String json, String sort, String order, String page) {
		List<ProposalModel> proposalModels = new ArrayList<>();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String createDateFrom = null;
		String createDateTo = null;
		List<Integer> proposalTypeIds = new ArrayList<>();

		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Map<String, Object> results = new TreeMap<String, Object>();
		try {
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			JsonNode jsonProposalTypeIds = jsonObject.get("proposalTypeIds");
			for (JsonNode proposalTypeId : jsonProposalTypeIds) {
				proposalTypeIds.add(Integer.valueOf(proposalTypeId.toString()));
			}
//			creator = (jsonObject.get("creator") != null && !jsonObject.get("creator").asText().equals("null")
//					&& jsonObject.get("creator").asInt() != 0) ? jsonObject.get("creator").asInt() : null;
			createDateFrom = (jsonObject.get("createDateFrom") != null
					&& !jsonObject.get("createDateFrom").asText().equals("null")
					&& !jsonObject.get("createDateFrom").asText().equals(""))
							? jsonObject.get("createDateFrom").asText()
							: null;
			createDateTo = (jsonObject.get("createDateTo") != null
					&& !jsonObject.get("createDateTo").asText().equals("null")
					&& !jsonObject.get("createDateTo").asText().equals("")) ? jsonObject.get("createDateTo").asText()
							: null;
			page = page == null ? "1" : page.trim();
			int limit = CMDConstrant.LIMIT;
			int offset = (Integer.parseInt(page) - 1) * limit;
			List<Integer> statusIds = new ArrayList<Integer>();
			for (JsonNode statusId : jsonStatusObject) {
				System.out.println(statusId.toString());
				statusIds.add(Integer.valueOf(statusId.toString()));
			}
			if (statusIds.size() == 0) {
				List<Status> statuses = statusRepositotyImpl.findAllForProposal();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
			}
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "pro.createDate";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			// creator alway null
			proposalModels = proposalRepositoryImpl.findAllProposalCratedByMe(userDetail.getId(), proposalTypeIds,
					statusIds, createDateFrom, createDateTo, sort, order, offset, limit);

//			Integer totalProposal  = 0;
//			totalProposal = proposalRepositoryImpl.countAllPaging(userDetail.getId(), proposal, content, status, creator, createDate, finishDate, sort, order, offset, limit);
//			
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(proposalRepositoryImpl.getCountAllForProposalCratedByMe());

			
			results.put("pagination", pagination);
			results.put("proposals", proposalModels);

			if (results.size() > 0) {
				// Count by status
				List<StatusModel> statusModels = new ArrayList<>();
				List<Status> statuses = statusRepositotyImpl.findAllForProposal();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}	
				
				List<ProposalModel> proposalModelForCount = proposalRepositoryImpl.findAllProposalCratedByMe(userDetail.getId(), new ArrayList<Integer>(),
						statusIds, null, null, sort, order, -1, -1);
				for (Status status : statuses) {
					int count = 0;
					for (ProposalModel pModel : proposalModelForCount) {
						if (pModel.getStatus().getId() == status.getId()) {
							count++;
						}
					}
					StatusModel statusModel = new StatusModel();
					statusModel.setId(status.getId());
					statusModel.setName(status.getName());
					statusModel.setCountByStatus(count);
					statusModels.add(statusModel);
				}
				results.put("countByStatuses", statusModels);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), results));
		}
	}

	public ResponseEntity<Object> findById(Integer id) {
		ProposalModel proposalModel = null;
		try {
			proposalModel = proposalRepositoryImpl.findModelById(id);
			if (null != proposalModel) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", proposalModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", ""));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> add(String json) {
		/* Affter add proposal:
		- Get proposal type id
		- Current step 
		Select all record from approval_steps with step index >= current step
		=> next select all record with approval_steps_id => fillter all employeeId from these record and save notify
		*/
		
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		ProposalModel proposalModel = null;
		List<ProposalDetail> proposalDetails = null;
		try {
			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObjectProposal;
			JsonNode jsonObjectProposalDetails;
			jsonObjectProposal = jsonMapper.readTree(json);
			jsonObjectProposalDetails = jsonObjectProposal.get("proposalDetails");
			String proposalTypeId = jsonObjectProposal.get("proposalTypeId") != null
					? jsonObjectProposal.get("proposalTypeId").asText()
					: "-1";
			Integer creatorId = userDetail.getId();
			Integer statusId = 1;
			String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			Proposal proposal = new Proposal();
			proposal.setCreateDate(createDate);
			proposal.setModifyDate(createDate);
			proposal.setCurrentStep(1);
			proposal.setModifyBy(userDetail.getId());

			ProposalType proposalType = proposalTypeRepositoryImpl.findById(proposalTypeId);
			proposal.setProposalType(proposalType);

			Employee creator = employeeRepositoryImpl.findById(creatorId);
			proposal.setCreator(creator);

			Status status = statusRepositotyImpl.findByIndexAndType(statusId,"proposal");
			proposal.setStatus(status);
			proposalDetails = new ArrayList<ProposalDetail>();
			for (JsonNode jsonObject : jsonObjectProposalDetails) {
				ProposalDetail proposalDetail = new ProposalDetail();
				String fieldId = jsonObject.get("fieldId") != null ? jsonObject.get("fieldId").asText() : "-1";
				String fieldName = jsonObject.get("fieldName") != null ? jsonObject.get("fieldName").asText() : "-1";
				String content = jsonObject.get("content") != null ? jsonObject.get("content").asText() : "-1";
				proposalDetail.setFieldId(fieldId);
				proposalDetail.setFieldName(fieldName);
				proposalDetail.setContent(content);
				proposalDetails.add(proposalDetail);
			}
			proposalModel = proposalRepositoryImpl.add(proposal, proposalDetails);

			if (null != proposalModel) {
				List<Integer> listEmpCanApprove = proposalRepositoryImpl.checkListCanApprove(proposalModel.getProposalType().getId(), proposalModel.getCurrentStep().toString(),creator.getId());
				if(listEmpCanApprove.contains(userDetail.getId())) {
				proposalModel.setCanApprove(true);
				}else {
					proposalModel.setCanApprove(false);
				}
				
				for(Integer empId : listEmpCanApprove) {
						if(empId!=-1) {
							Employee employee = employeeRepositoryImpl.findById(empId);	
							Notify notify = null;
							notify = new Notify();
							notify.setIsRead(false);
							notify.setReceiver(employee);
							notify.setTitle("Đề xuất mới");
							notify.setDescription("Bạn vừa nhận được đề xuất từ "+ proposalModel.getCreator().getName());
							notify.setType("proposal");
							notify.setDetailId(proposal.getId());
							notifyRepositoryImpl.add(notify);
						}
					System.out.println("Save notify sucessfully");
				}
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Thêm đề xuất thành công", proposalModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Thêm đề xuất thất bại", proposalModel));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), proposalModel));
		}
	}

	public ResponseEntity<Object> edit(String json) {
		ProposalModel proposalModel = null;
		List<ProposalDetail> proposalDetails = null;
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		try {
			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObjectProposal;
			JsonNode jsonObjectProposalDetails;
			jsonObjectProposal = jsonMapper.readTree(json);
			jsonObjectProposalDetails = jsonObjectProposal.get("proposalDetails");
			Integer proposalId = jsonObjectProposal.get("id") != null ? jsonObjectProposal.get("id").asInt() : -1;
			String modifydate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			Proposal proposal = proposalRepositoryImpl.findById(proposalId);
			proposal.setModifyDate(modifydate);
			proposal.setModifyBy(userDetail.getId());
			proposalDetails = new ArrayList<ProposalDetail>();
			for (JsonNode jsonObject : jsonObjectProposalDetails) {
				ProposalDetail proposalDetail = new ProposalDetail();
				Integer id = jsonObject.get("id") != null ? jsonObject.get("id").asInt() : -1;
				String fieldId = jsonObject.get("fieldId") != null ? jsonObject.get("fieldId").asText() : "-1";
				String fieldName = jsonObject.get("fieldName") != null ? jsonObject.get("fieldName").asText() : "-1";
				String content = jsonObject.get("content") != null ? jsonObject.get("content").asText() : "-1";
				proposalDetail.setId(id);
				proposalDetail.setFieldId(fieldId);
				proposalDetail.setFieldName(fieldName);
				proposalDetail.setContent(content);
				proposalDetails.add(proposalDetail);
			}

			proposalModel = proposalRepositoryImpl.edit(proposal, proposalDetails);

			if (proposalModel != null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Cập nhật đề xuất thành công", proposalModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Cập nhật đề xuất thất bại", ""));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> accept(Integer proposalId) {
		ProposalModel proposalModel = null;
		Proposal proposal = proposalRepositoryImpl.findById(proposalId);
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Employee userEmp = employeeRepositoryImpl.findById(userDetail.getId());
//		Status status = statusRepositotyImpl.findByIndexAndType(totalStep, null)
		if (proposal != null) {
			Integer totalStep = approvalStepRepository.countByProposalTypeId(proposal.getProposalType().getId());
			Status curentStatus = proposal.getStatus();
			// if next step still in total step, current step will be update to next step
			// and keep status is pending
			// else "currentStep" will always be total + 1 and change status to complete
			if (curentStatus.getType().equals("proposal")
					&& (curentStatus.getIndex() == CMDConstrant.PROPOSAL_COMPLETE_STATUS_INDEX
							|| curentStatus.getIndex() == CMDConstrant.PROPOSAL_CANCELLED_STATUS_INDEX
							|| curentStatus.getIndex() == CMDConstrant.PROPOSAL_DENIED_STATUS_INDEX)) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Đề xuất đã hoàn thành hoặc đã bị từ chối", ""));
			}
			Integer nextStep = proposal.getCurrentStep() + 1;
			if (nextStep <= totalStep) {
				proposal.setCurrentStep(nextStep);
			} else {
				proposal.setCurrentStep(totalStep + 1);
				Status newStatus = statusRepositotyImpl.findByIndexAndType(CMDConstrant.PROPOSAL_COMPLETE_STATUS_INDEX,
						"proposal");
				proposal.setStatus(newStatus);
			}
			proposalModel = proposalRepositoryImpl.edit(proposal, null);
			if (proposalModel!=null) {
				List<Integer> listEmpCanApprove = proposalRepositoryImpl.checkListCanApprove(proposalModel.getProposalType().getId(), proposalModel.getCurrentStep().toString(),proposalModel.getCreator().getId());
				if(listEmpCanApprove.contains(userDetail.getId())) {
				proposalModel.setCanApprove(true);
				}else {
					proposalModel.setCanApprove(false);
				}
				
				for(Integer empId : listEmpCanApprove) {
					if(empId!=-1) {
						Employee employee = employeeRepositoryImpl.findById(empId);
						Notify notify = null;
						notify = new Notify();
						notify.setIsRead(false);
						notify.setReceiver(employee);
						notify.setTitle("Duyệt đề xuất");
						notify.setDescription("Đề xuất vừa được duyệt bởi "+ userEmp.getName());
						notify.setType("proposal");
						notify.setDetailId(proposal.getId());
						notifyRepositoryImpl.add(notify);
					}
				}
				// Save notify for creator
				if(!listEmpCanApprove.contains(proposalModel.getCreator().getId())) {
					Employee employee = employeeRepositoryImpl.findById(proposalModel.getCreator().getId());
					Notify notify = null;
					notify = new Notify();
					notify.setIsRead(false);
					notify.setReceiver(employee);
					notify.setTitle("Duyệt đề xuất");
					notify.setDescription("Đề xuất vừa được duyệt bởi "+ userEmp.getName());
					notify.setType("proposal");
					notify.setDetailId(proposal.getId());
					notifyRepositoryImpl.add(notify);
				}
				
				// Response data for FE to show
				proposalModel = proposalRepositoryImpl.findModelById(proposalId);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Cập nhật đề xuất thành công", proposalModel));
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Có lỗi xảy ra khi lưu thông báo", ""));

	}

	public ResponseEntity<Object> denied(String json) {
		ProposalModel proposalModel = null;
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Employee userEmp = employeeRepositoryImpl.findById(userDetail.getId());
		Integer id = -1;
		String reason = "";
		try {
			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObjectProposal;
			jsonObjectProposal = jsonMapper.readTree(json);
			id = jsonObjectProposal.get("id") != null ? jsonObjectProposal.get("id").asInt() : -1;
			reason = jsonObjectProposal.get("reason") != null ? jsonObjectProposal.get("reasonl").asText() : "";
		} catch (Exception e) {
			// TODO: handle exception
			LOGGER.error(e.getMessage());
		}
		Proposal proposal = proposalRepositoryImpl.findById(id);
		if (proposal != null) {
			Status curentStatus = proposal.getStatus();
			// if next step still in total step, current step will be update to next step
			// and keep status is pending
			// else "currentStep" will always be total + 1 and change status to complete
			if (curentStatus.getType().equals("proposal")
					&& (curentStatus.getIndex() == CMDConstrant.PROPOSAL_COMPLETE_STATUS_INDEX
							|| curentStatus.getIndex() == CMDConstrant.PROPOSAL_CANCELLED_STATUS_INDEX
							|| curentStatus.getIndex() == CMDConstrant.PROPOSAL_DENIED_STATUS_INDEX)) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Đề xuất đã hoàn thành hoặc đã bị từ chối", ""));
			}
			Status newStatus = statusRepositotyImpl.findByIndexAndType(CMDConstrant.PROPOSAL_DENIED_STATUS_INDEX,
					"proposal");
			proposal.setStatus(newStatus);
			proposal.setReason(reason);
			proposalModel = proposalRepositoryImpl.edit(proposal, null);
			if (proposalModel != null) {
				List<Integer> listEmpCanApprove = proposalRepositoryImpl.checkListCanApprove(proposalModel.getProposalType().getId(), proposalModel.getCurrentStep().toString(),proposalModel.getCreator().getId());
				if(listEmpCanApprove.contains(userDetail.getId())) {
				proposalModel.setCanApprove(true);
				}else {
					proposalModel.setCanApprove(false);
				}
				
				for(Integer empId : listEmpCanApprove) {
					if(empId!=-1) {
						Employee employee = employeeRepositoryImpl.findById(empId);
						Notify notify = null;
						notify = new Notify();
						notify.setIsRead(false);
						notify.setReceiver(employee);
						notify.setTitle("Từ chối đề xuất");
						notify.setDescription("Đề xuất bị từ chối bởi "+ userEmp.getName());
						notify.setType("proposal");
						notify.setDetailId(proposal.getId());
						notifyRepositoryImpl.add(notify);
					}
				}
				// Response data for FE to show
				proposalModel = proposalRepositoryImpl.findModelById(id);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Cập nhật đề xuất thành công", proposalModel));
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Có lỗi xảy ra", ""));
	}

	public ResponseEntity<Object> cancel(String json) {
		ProposalModel proposalModel = null;
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Employee userEmp = employeeRepositoryImpl.findById(userDetail.getId());
		Integer id = -1;
		String reason = "";
		try {
			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObjectProposal;
			jsonObjectProposal = jsonMapper.readTree(json);
			id = jsonObjectProposal.get("id") != null ? jsonObjectProposal.get("id").asInt() : -1;
			reason = jsonObjectProposal.get("reason") != null ? jsonObjectProposal.get("reason").asText() : "";
		} catch (Exception e) {
			// TODO: handle exception
			LOGGER.error(e.getMessage());
		}
		Proposal proposal = proposalRepositoryImpl.findById(id);
		if (proposal != null) {
			Status curentStatus = proposal.getStatus();
			// if next step still in total step, current step will be update to next step
			// and keep status is pending
			// else "currentStep" will always be total + 1 and change status to complete
			if (curentStatus.getType().equals("proposal")
					&& (curentStatus.getIndex() == CMDConstrant.PROPOSAL_COMPLETE_STATUS_INDEX
							|| curentStatus.getIndex() == CMDConstrant.PROPOSAL_CANCELLED_STATUS_INDEX
							|| curentStatus.getIndex() == CMDConstrant.PROPOSAL_DENIED_STATUS_INDEX)) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Đề xuất đã hoàn thành hoặc đã bị từ chối", ""));
			}
			Status newStatus = statusRepositotyImpl.findByIndexAndType(CMDConstrant.PROPOSAL_CANCELLED_STATUS_INDEX,
					"proposal");
			proposal.setStatus(newStatus);
			proposal.setReason(reason);
			proposalModel = proposalRepositoryImpl.edit(proposal, null);
			if ( proposalModel!=null) {
				List<Integer> listEmpCanApprove = proposalRepositoryImpl.checkListCanApprove(proposalModel.getProposalType().getId(), proposalModel.getCurrentStep().toString(),proposalModel.getCreator().getId());
				if(listEmpCanApprove.contains(userDetail.getId())) {
				proposalModel.setCanApprove(true);
				}else {
					proposalModel.setCanApprove(false);
				}
				for(Integer empId : listEmpCanApprove) {
					if(empId!=-1) {
						Employee employee = employeeRepositoryImpl.findById(empId);
						Notify notify = null;
						notify = new Notify();
						notify.setIsRead(false);
						notify.setReceiver(employee);
						notify.setTitle("Hủy đề xuất");
						notify.setDescription("Đề xuất đã bị hủy bởi "+ userEmp.getName());
						notify.setType("proposal");
						notify.setDetailId(proposal.getId());
						notifyRepositoryImpl.add(notify);
					}
				}
				// Response data for FE to show
				proposalModel = proposalRepositoryImpl.findModelById(id);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Cập nhật đề xuất thành công", proposalModel));
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Có lỗi xảy ra", ""));
	}

}
