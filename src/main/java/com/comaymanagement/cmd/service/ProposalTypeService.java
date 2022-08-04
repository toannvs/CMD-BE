package com.comaymanagement.cmd.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.ProposalTypeModel;
import com.comaymanagement.cmd.repositoryimpl.DepartmentRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.PositionRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalTypeRepositoryImpl;

@Service
public class ProposalTypeService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	Message message;
	@Autowired
	ProposalTypeRepositoryImpl proposalTypeRepository;
	@Autowired
	DepartmentRepositoryImpl departmentRepository;

	@Autowired
	PositionRepositoryImpl positionRepository;
	
	// findAll for config
	public ResponseEntity<Object> findAllConfig(){
		List<ProposalType> proposalTypes = proposalTypeRepository.findAll();
		List<ProposalTypeModel> proposalTypeModels = new ArrayList<>();
		for(ProposalType proposalType : proposalTypes) {
			proposalTypeModels.add(proposalTypeRepository.toModel(proposalType));
		}
		if(proposalTypes==null) {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR","Có lỗi xảy ra trong quá trình tìm kiếm","" ));
		}
		if(proposalTypes!=null && proposalTypes.size()>0) {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "",proposalTypeModels ));
		}else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "Not found","" ));
		}
		
	}
	
	public ResponseEntity<Object> findAllWithPermission(){
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Integer employeeId = userDetail.getId();
		List<Integer> positionIds = new ArrayList<>();
		List<Integer> departmentIds = new ArrayList<>();
		List<Position> positionTMPs = positionRepository.findAllByEmployeeId(employeeId);
		List<Department> departmentTMPs = departmentRepository.findAllByEmployeeId(employeeId);
		for(Department d : departmentTMPs) {
			departmentIds.add(d.getId());
		}
		for(Position p : positionTMPs) {
			positionIds.add(p.getId());
		}
		List<ProposalType> proposalTypes = proposalTypeRepository.findProposalPermission(employeeId, positionIds, departmentIds);
		List<ProposalType> proposalTypeEnableAll = proposalTypeRepository.findProposalEnableAll();
		if(proposalTypes==null) {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR","Có lỗi xảy ra trong quá trình tìm kiếm","" ));
		}
		if(proposalTypes!=null && proposalTypes.size()>0) {
			proposalTypes.addAll(proposalTypeEnableAll);
			List<ProposalTypeModel> proposalTypeModels = new ArrayList<>();
			for(ProposalType proType : proposalTypes) {
				proposalTypeModels.add(proposalTypeRepository.toModel(proType));
			}
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "",proposalTypeModels ));
		}else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "Not found","" ));
		}
		
		
	}
	
}
