package com.comaymanagement.cmd.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.ApprovalOption_View;
import com.comaymanagement.cmd.entity.ApprovalStep;
import com.comaymanagement.cmd.entity.ApprovalStepDetail;
import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.ApprovalStepModel;
import com.comaymanagement.cmd.repositoryimpl.ApprovalOption_ViewRepository;
import com.comaymanagement.cmd.repositoryimpl.ApprovalStepDetailRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ApprovalStepRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalTypeRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApprovalStepService {
	@Autowired
	Message message;
	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalStepService.class);
	@Autowired
	ProposalTypeRepositoryImpl proposalTypeRepository;
	@Autowired
	ApprovalStepRepositoryImpl approvalStepRepository;
	@Autowired
	ApprovalStepDetailRepositoryImpl approvalStepDetailRepository;
	@Autowired
	ApprovalOption_ViewRepository approvalOptionReposiroty;
	
	public ResponseEntity<Object> findAllByProposalId(Integer proposalTypeId) {
			
		List<ApprovalStepModel> approvalStepModels = approvalStepRepository.toModel(approvalStepRepository.findByProposalTypeId(proposalTypeId));
		List<ApprovalStepDetail> tmp = new ArrayList<>();
		List<ApprovalOption_View> approvalOptionViews;
		for(ApprovalStepModel approvalStepModel : approvalStepModels) {
			tmp  = approvalStepDetailRepository.findAllByApprovalStepId(approvalStepModel.getId());
			approvalOptionViews = new ArrayList<>();
			for(ApprovalStepDetail appStepDetail : tmp) {
				// have list of all emp or dep or position in all step of proposal
//				approvalStepDetails.add(appStepDetail);
				ApprovalOption_View approvalOptionEmp = approvalOptionReposiroty.findById(appStepDetail.getEmployeeId(),"employees");
				ApprovalOption_View approvalOptionDep  = approvalOptionReposiroty.findById(appStepDetail.getDepartmentId(),"departments");
				ApprovalOption_View approvalOptionPos  = approvalOptionReposiroty.findById(appStepDetail.getPositionId(),"positions");
				if(approvalOptionEmp!=null) {
					approvalOptionViews.add(approvalOptionEmp);
				}
				if(approvalOptionDep!=null) {
					approvalOptionViews.add(approvalOptionDep);
				}
				if(approvalOptionPos!=null) {
					approvalOptionViews.add(approvalOptionPos);
				}
				
			}
			tmp.clear();
			approvalStepModel.setApprovalConfigTargets(approvalOptionViews);
		}
			if (approvalStepModels!=null ) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("OK", "OK", approvalStepModels));
			}else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Có lỗi xảy ra", ""));
			}
			
	}
	
	public ResponseEntity<Object> add(String json) {
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectApprovalStep;
		JsonNode jsonObjectStep;
		JsonNode jsonObjectApprovalConfigTarget;
		List<Integer> empIds;
		List<Integer> depIds;
		List<Integer> posIds;
		List<ApprovalStepDetail> approvalStepDetails;
		List<ApprovalOption_View> approvalOptionViews;
		try {
			jsonObjectApprovalStep = jsonMapper.readTree(json);
			jsonObjectStep = jsonObjectApprovalStep.get("step");
			jsonObjectApprovalConfigTarget = jsonObjectStep.get("approvalConfigTargets");
			Integer proposalTypeId = jsonObjectApprovalStep.get("id").asInt();
			ProposalType proposalType = proposalTypeRepository.findById(String.valueOf(proposalTypeId));
			String index = jsonObjectStep.get("index").asText();
			String name = jsonObjectStep.get("name").asText();
			// save approvalStep
			ApprovalStep approvalStep = new ApprovalStep();
			approvalStep.setApprovalStepIndex(Integer.valueOf(index));
			approvalStep.setApprovalStepName(name);
			approvalStep.setProposalType(proposalType);
			if (approvalStepRepository.add(approvalStep) > 0) {
				empIds = new ArrayList<>();
				depIds = new ArrayList<>();
				posIds = new ArrayList<>();
				for (JsonNode approvalConfigTarget : jsonObjectApprovalConfigTarget) {
					Integer id = approvalConfigTarget.get("id").asInt();
					String table = approvalConfigTarget.get("table").asText();
					// Add too list to caculator number object will be created
					if (table.equals("employees")) {
						empIds.add(id);
					} else if (table.equals("departments")) {
						depIds.add(id);
					} else {
						posIds.add(id);
					}
				}
				approvalStepDetails = new ArrayList<>();
				int countObjectNumber = (empIds.size() + depIds.size() + posIds.size()) / 3
						+ (empIds.size() + depIds.size() + posIds.size()) % 3;
				for (int i = 0; i < countObjectNumber; i++) {
					// Set default value -1
					ApprovalStepDetail approvalStepDetail = new ApprovalStepDetail();
					approvalStepDetail.setApprovalStep(approvalStep);
					approvalStepDetail.setEmployeeId(-1);
					approvalStepDetail.setDepartmentId(-1);
					approvalStepDetail.setPositionId(-1);
					approvalStepDetails.add(approvalStepDetail);
				}
				// set real value
				for (int i = 0; i < empIds.size(); i++) {
					approvalStepDetails.get(i).setEmployeeId(empIds.get(i));
				}
				for (int i = 0; i < depIds.size(); i++) {
					approvalStepDetails.get(i).setDepartmentId(depIds.get(i));
				}
				for (int i = 0; i < posIds.size(); i++) {
					approvalStepDetails.get(i).setPositionId(posIds.get(i));
				}
				// save list approvalStepDetail
				for (ApprovalStepDetail approvalStepDetail : approvalStepDetails) {
					if (approvalStepDetailRepository.add(approvalStepDetail) < 0) {
						return ResponseEntity.status(HttpStatus.OK)
								.body(new ResponseObject("ERROR", "Thêm bước duyệt thất bại", ""));
					}
				}
				
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Thêm bước duyệt thất bại", ""));
			}
			
			approvalOptionViews = new ArrayList<>();
			for (ApprovalStepDetail appStepDetail : approvalStepDetails) {
				// have list of all emp or dep or position in all step of proposal
				ApprovalOption_View approvalOptionEmp = approvalOptionReposiroty.findById(appStepDetail.getEmployeeId(),
						"employees");
				ApprovalOption_View approvalOptionDep = approvalOptionReposiroty
						.findById(appStepDetail.getDepartmentId(), "departments");
				ApprovalOption_View approvalOptionPos = approvalOptionReposiroty.findById(appStepDetail.getPositionId(),
						"positions");
				if (approvalOptionEmp != null) {
					approvalOptionViews.add(approvalOptionEmp);
				}
				if (approvalOptionDep != null) {
					approvalOptionViews.add(approvalOptionDep);
				}
				if (approvalOptionPos != null) {
					approvalOptionViews.add(approvalOptionPos);
				}
				
			}
			ApprovalStepModel approvalStepModel = new ApprovalStepModel();
			approvalStepModel.setId(approvalStep.getId());
			approvalStepModel.setIndex(approvalStep.getApprovalStepIndex());
			approvalStepModel.setName(approvalStep.getApprovalStepName());
			approvalStepModel.setApprovalConfigTargets(approvalOptionViews);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "Thêm bước duyệt thành công", approvalStepModel));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Thêm bước duyệt thất bại", ""));
		}
	}

	
	// After delete must update all step index
	public ResponseEntity<Object> delete(Integer id) {
		List<ApprovalStepDetail> approvalStepDetails = approvalStepDetailRepository.findAllByApprovalStepId(id);
		ApprovalStep approvalStep = approvalStepRepository.findById(id);
		for (ApprovalStepDetail approvalStepDetail : approvalStepDetails) {
			if (approvalStepDetailRepository.delete(approvalStepDetail.getId()) < 0) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Có lỗi xảy ra trong quá trình xóa", ""));
			}
		}
		if(approvalStepRepository.delete(id) < 0 ) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Có lỗi xảy ra trong quá trình xóa", ""));
		}else {
			// Update index 
			List<ApprovalStep> approvalSteps = approvalStepRepository.findByProposalTypeId(approvalStep.getProposalType().getId());
			for(int i=0;i<approvalSteps.size();i++) {
				ApprovalStep approStep = approvalSteps.get(i);
				approStep.setApprovalStepIndex(i+1);
				approvalStepRepository.edit(approStep);
			}
			// Prepare response data
			List<ApprovalStepModel>	approvalStepModels = approvalStepRepository.toModel(approvalSteps);
			List<ApprovalStepDetail> tmp = new ArrayList<>();
			for(ApprovalStepModel approvalStepModel : approvalStepModels) {
				tmp  = approvalStepDetailRepository.findAllByApprovalStepId(approvalStepModel.getId());
				List<ApprovalOption_View> approvalOptionViews = new ArrayList<>();
				for(ApprovalStepDetail appStepDetail : tmp) {
					// have list of all emp or dep or position in all step of proposal
//					approvalStepDetails.add(appStepDetail);
					ApprovalOption_View approvalOptionEmp = approvalOptionReposiroty.findById(appStepDetail.getEmployeeId(),"employees");
					ApprovalOption_View approvalOptionDep  = approvalOptionReposiroty.findById(appStepDetail.getDepartmentId(),"departments");
					ApprovalOption_View approvalOptionPos  = approvalOptionReposiroty.findById(appStepDetail.getPositionId(),"positions");
					if(approvalOptionEmp!=null) {
						approvalOptionViews.add(approvalOptionEmp);
					}
					if(approvalOptionDep!=null) {
						approvalOptionViews.add(approvalOptionDep);
					}
					if(approvalOptionPos!=null) {
						approvalOptionViews.add(approvalOptionPos);
					}
					
				}
				tmp.clear();
				approvalStepModel.setApprovalConfigTargets(approvalOptionViews);
			}
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Xóa bước duyệt thành công",approvalStepModels));
		}
		
	}
	
	public ResponseEntity<Object> edit(String json) {
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectApprovalStep;
		JsonNode jsonObjectStep;
		JsonNode jsonObjectApprovalConfigTarget;
		List<Integer> empIds;
		List<Integer> depIds;
		List<Integer> posIds;
		List<ApprovalStepDetail> approvalStepDetails;
		List<ApprovalOption_View> approvalOptionViews;
		try {
			jsonObjectApprovalStep = jsonMapper.readTree(json);
			jsonObjectStep = jsonObjectApprovalStep.get("step");
			jsonObjectApprovalConfigTarget = jsonObjectStep.get("approvalConfigTargets");
//			Integer proposalTypeId = jsonObjectApprovalStep.get("id").asInt();
			Integer stepId = jsonObjectStep.get("id").asInt();
//			ProposalType proposalType = proposalTypeRepository.findById(String.valueOf(proposalTypeId));
			String name = jsonObjectStep.get("name").asText();
			
			// find approvalStep edit
			ApprovalStep approvalStepEdit = approvalStepRepository.findById(stepId);
			
			
			// save approvalStep
			approvalStepEdit.setApprovalStepName(name);
			if (approvalStepRepository.edit(approvalStepEdit) > 0) {
				// delete all approval step detail old
				List<ApprovalStepDetail> approvalStepDetailOlds = approvalStepDetailRepository.findAllByApprovalStepId(stepId);
				if(approvalStepDetailOlds!=null && approvalStepDetailOlds.size()>0) {
					for(ApprovalStepDetail appStepDetail : approvalStepDetailOlds) {
						approvalStepDetailRepository.delete(appStepDetail.getId());
					}
				}
				empIds = new ArrayList<>();
				depIds = new ArrayList<>();
				posIds = new ArrayList<>();
				for (JsonNode approvalConfigTarget : jsonObjectApprovalConfigTarget) {
					Integer id = approvalConfigTarget.get("id").asInt();
					String table = approvalConfigTarget.get("table").asText();
					// Add too list to caculator number object will be created
					if (table.equals("employees")) {
						empIds.add(id);
					} else if (table.equals("departments")) {
						depIds.add(id);
					} else {
						posIds.add(id);
					}
				}
				approvalStepDetails = new ArrayList<>();
				int countObjectNumber = Math.max(Math.max(empIds.size(), depIds.size()), posIds.size());
				for (int i = 0; i < countObjectNumber; i++) {
					// Set default value -1
					ApprovalStepDetail approvalStepDetail = new ApprovalStepDetail();
					approvalStepDetail.setApprovalStep(approvalStepEdit);
					approvalStepDetail.setEmployeeId(-1);
					approvalStepDetail.setDepartmentId(-1);
					approvalStepDetail.setPositionId(-1);
					approvalStepDetails.add(approvalStepDetail);
				}
				// set real value
				for (int i = 0; i < empIds.size(); i++) {
					approvalStepDetails.get(i).setEmployeeId(empIds.get(i));
				}
				for (int i = 0; i < depIds.size(); i++) {
					approvalStepDetails.get(i).setDepartmentId(depIds.get(i));
				}
				for (int i = 0; i < posIds.size(); i++) {
					approvalStepDetails.get(i).setPositionId(posIds.get(i));
				}
				
			
				// save list approval step detail new
				for (ApprovalStepDetail approvalStepDetail : approvalStepDetails) {
					if (approvalStepDetailRepository.add(approvalStepDetail) < 0) {
						return ResponseEntity.status(HttpStatus.OK)
								.body(new ResponseObject("ERROR", "Thêm bước duyệt thất bại", ""));
					}
				}

			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Thêm bước duyệt thất bại", ""));
			}

			// Response data
			approvalOptionViews = new ArrayList<>();
			for (ApprovalStepDetail appStepDetail : approvalStepDetails) {
				// have list of all emp or dep or position in all step of proposal
				ApprovalOption_View approvalOptionEmp = approvalOptionReposiroty.findById(appStepDetail.getEmployeeId(),
						"employees");
				ApprovalOption_View approvalOptionDep = approvalOptionReposiroty
						.findById(appStepDetail.getDepartmentId(), "departments");
				ApprovalOption_View approvalOptionPos = approvalOptionReposiroty.findById(appStepDetail.getPositionId(),
						"positions");
				if (approvalOptionEmp != null) {
					approvalOptionViews.add(approvalOptionEmp);
				}
				if (approvalOptionDep != null) {
					approvalOptionViews.add(approvalOptionDep);
				}
				if (approvalOptionPos != null) {
					approvalOptionViews.add(approvalOptionPos);
				}

			}
			ApprovalStepModel approvalStepModel = new ApprovalStepModel();
			approvalStepModel.setId(approvalStepEdit.getId());
			approvalStepModel.setIndex(approvalStepEdit.getApprovalStepIndex());
			approvalStepModel.setName(approvalStepEdit.getApprovalStepName());
			approvalStepModel.setApprovalConfigTargets(approvalOptionViews);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "Thêm bước duyệt thành công", approvalStepModel));
		} catch (Exception e) {
			LOGGER.error(e.getStackTrace().toString());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Thêm bước duyệt thất bại", ""));
		}
	}
}
