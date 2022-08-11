package com.comaymanagement.cmd.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.ApprovalOption_View;
import com.comaymanagement.cmd.entity.ApprovalStep;
import com.comaymanagement.cmd.entity.ApprovalStepDetail;
import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.ProposalTypeDetail;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.ApprovalStepModel;
import com.comaymanagement.cmd.model.ProposalTypeDetailModel;
import com.comaymanagement.cmd.repositoryimpl.ApprovalOption_ViewRepository;
import com.comaymanagement.cmd.repositoryimpl.ApprovalStepDetailRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ApprovalStepRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalTypeDetailRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalTypeRepositoryImpl;

@Service
public class ProposalTypeDetailService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	Message message;
	
	@Autowired
	ProposalTypeRepositoryImpl proposalTypeRepository;
	
	@Autowired
	ProposalTypeDetailRepositoryImpl proposalTypeDetailReposiotory;
	@Autowired
	ApprovalStepRepositoryImpl approvalStepRepository;
	@Autowired
	ApprovalStepDetailRepositoryImpl approvalStepDetailRepository;
	@Autowired
	ApprovalOption_ViewRepository approvalOptionReposiroty;
	public ResponseEntity<Object> findById(Integer id){
		List<ProposalTypeDetail> proposalTypeDetails= new ArrayList<>();
		List<ApprovalStep> approvalSteps = new ArrayList<>();
		List<ProposalTypeDetailModel> proposalTypeDetailModels = new ArrayList<>();
		List<ApprovalStepModel> approvalStepModels = new ArrayList<>();
//		List<ApprovalStepDetail> approvalStepDetails = new ArrayList<>();
		List<ApprovalOption_View> approvalOptionViews;
		approvalSteps = approvalStepRepository.findByProposalTypeId(id);
		proposalTypeDetails = proposalTypeDetailReposiotory.findById(id);
		List<ApprovalStepDetail> tmp = new ArrayList<>();
		if (proposalTypeDetails!=null && approvalSteps !=null) {
			proposalTypeDetailModels = proposalTypeDetailReposiotory.toModel(proposalTypeDetails);
			approvalStepModels = approvalStepRepository.toModel(approvalSteps);
			// have list of all emp or dep or position in all step of proposal
			for(ApprovalStepModel approvalStepModel : approvalStepModels) {
				tmp  = approvalStepDetailRepository.findAllByApprovalStepId(approvalStepModel.getId());
				approvalOptionViews = new ArrayList<>();
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
			ProposalType proposalType = proposalTypeRepository.findById(id.toString());
			
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("id", proposalType.getId());
			result.put("name", proposalType.getName());
			result.put("activeFlag", proposalType.isActiveFlag());
			result.put("createDate", proposalType.getCreateDate());
			result.put("fields", proposalTypeDetailModels);
			result.put("steps", approvalStepModels);
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "",result ));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Có lỗi xảy ra", ""));
		}
	}
	
}
