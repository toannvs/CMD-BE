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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.ApprovalOption_View;
import com.comaymanagement.cmd.entity.ApprovalStep;
import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.ProposalPermission;
import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.ProposalTypeDetail;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.ApprovalStepModel;
import com.comaymanagement.cmd.model.ProposalTypeDetailModel;
import com.comaymanagement.cmd.model.ProposalTypeModel;
import com.comaymanagement.cmd.repositoryimpl.ApprovalOption_ViewRepository;
import com.comaymanagement.cmd.repositoryimpl.ApprovalStepRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.DepartmentRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.PositionRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalPermissionImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalTypeDetailRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalTypeRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

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

	@Autowired
	ProposalPermissionImpl proposalPermissionRepository;
	@Autowired
	ApprovalOption_ViewRepository approvalOptionReposiroty;
	@Autowired
	ProposalTypeDetailRepositoryImpl proposalTypeDetailReposiotory;
	@Autowired
	ApprovalStepRepositoryImpl approvalStepRepository;
	// findAll for config
	public ResponseEntity<Object> findAllConfig() {
//		List<ApprovalOption_View> approvalOptionViews;

		List<ProposalType> proposalTypes = new ArrayList<>();
		proposalTypes = proposalTypeRepository.findAll();
		List<ProposalTypeModel> proposalTypeModels = new ArrayList<>();
		List<ProposalTypeDetailModel> proposalTypeDetailModels = new ArrayList<>();
		List<ProposalTypeDetail> proposalTypeDetails = new ArrayList<>();
		List<Map<String, Object>> results = new ArrayList<>();
		for (ProposalType proposalType : proposalTypes) {
			List<ApprovalStep> approvalSteps = new ArrayList<>();
			List<ApprovalStepModel> approvalStepModels = new ArrayList<>();
			approvalSteps = approvalStepRepository.findByProposalTypeId(proposalType.getId());
			approvalStepModels = approvalStepRepository.toModel(approvalSteps);
			proposalTypeDetails = proposalTypeDetailReposiotory.findById(proposalType.getId());
			proposalTypeDetailModels = proposalTypeDetailReposiotory.toModel(proposalTypeDetails);
			proposalTypeModels.add(proposalTypeRepository.toModel(proposalType));
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("id", proposalType.getId());
			result.put("name", proposalType.getName());
			result.put("activeFlag", proposalType.isActiveFlag());
			result.put("createDate", proposalType.getCreateDate());
			result.put("fields", proposalTypeDetailModels);
			result.put("steps", approvalStepModels);
			results.add(result);
		}
		if (proposalTypes.size() > 0) {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", results));
		} else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Not found", results));

		}

	}

	public ResponseEntity<Object> findAllWithPermission() {
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Integer employeeId = userDetail.getId();
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
		List<ProposalType> proposalTypes = proposalTypeRepository.findProposalPermission(employeeId, positionIds,
				departmentIds);
		List<ProposalType> proposalTypeEnableAll = proposalTypeRepository.findProposalEnableAll();
		if (proposalTypes == null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Có lỗi xảy ra trong quá trình tìm kiếm", ""));
		}
		proposalTypes.addAll(proposalTypeEnableAll);
		List<ProposalTypeModel> proposalTypeModels = new ArrayList<>();
		List<ProposalTypeDetailModel> proposalTypeDetailModels = new ArrayList<>();
		List<ProposalTypeDetail> proposalTypeDetails = new ArrayList<>();
		List<Map<String, Object>> results = new ArrayList<>();
		if (proposalTypes != null && proposalTypes.size() > 0) {
			
			for (ProposalType proposalType : proposalTypes) {
//				List<ApprovalStep> approvalSteps = new ArrayList<>();
//				List<ApprovalStepModel> approvalStepModels = new ArrayList<>();
//				approvalSteps = approvalStepRepository.findByProposalTypeId(proposalType.getId());
//				approvalStepModels = approvalStepRepository.toModel(approvalSteps);
				proposalTypeDetails = proposalTypeDetailReposiotory.findById(proposalType.getId());
				proposalTypeDetailModels = proposalTypeDetailReposiotory.toModel(proposalTypeDetails);
				proposalTypeModels.add(proposalTypeRepository.toModel(proposalType));
				Map<String, Object> result = new LinkedHashMap<>();
				result.put("id", proposalType.getId());
				result.put("name", proposalType.getName());
				result.put("activeFlag", proposalType.isActiveFlag());
				result.put("createDate", proposalType.getCreateDate());
				result.put("fields", proposalTypeDetailModels);
//				result.put("steps", approvalStepModels);
				results.add(result);
			}
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", results));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "Not found", proposalTypeModels));
		}

	}

	public ResponseEntity<Object> updateAndGantPermission(String json) {
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectProposalType;
		JsonNode jsonObjectProposalConfigTargets;
		List<Integer> empIds;
		List<Integer> depIds;
		List<Integer> posIds;
		List<ProposalPermission> proposalPermissionSaves = new ArrayList<>();
		;
		List<ApprovalOption_View> approvalOptionViews;
		try {
			jsonObjectProposalType = jsonMapper.readTree(json);
			Integer proposalTypeId = jsonObjectProposalType.get("id") != null ? jsonObjectProposalType.get("id").asInt()
					: -1;
			String proposalTypeName = jsonObjectProposalType.get("name") != null
					? jsonObjectProposalType.get("name").asText()
					: "";
			jsonObjectProposalConfigTargets = jsonObjectProposalType.get("proposalConfigTargets");
			ProposalType proposalType = proposalTypeRepository.findById(proposalTypeId.toString());
			if (proposalType != null) {
				proposalType.setName(proposalTypeName);
				if (jsonObjectProposalConfigTargets != null) {
					// delete all old permission
					List<ProposalPermission> proposalPermissionOlds = proposalPermissionRepository
							.findAllByProposalTypeId(proposalTypeId);
					if (proposalPermissionOlds != null && proposalPermissionOlds.size() > 0) {
						for (ProposalPermission permission : proposalPermissionOlds) {
							proposalPermissionRepository.delete(permission);
						}
					}
					// if size < 0 => do nothing
					if (jsonObjectProposalConfigTargets.size() > 0) {
						empIds = new ArrayList<>();
						depIds = new ArrayList<>();
						posIds = new ArrayList<>();
						for (JsonNode proposalConfigTargets : jsonObjectProposalConfigTargets) {
							Integer id = proposalConfigTargets.get("id").asInt();
							String table = proposalConfigTargets.get("table").asText();
							// Add too list to caculator number object will be created
							if (table.equals("employees")) {
								empIds.add(id);
							} else if (table.equals("departments")) {
								depIds.add(id);
							} else {
								posIds.add(id);
							}
						}
						int countObjectNumber = Math.max(Math.max(empIds.size(), depIds.size()), posIds.size());
						for (int i = 0; i < countObjectNumber; i++) {
							// Set default value -1
							ProposalPermission proposalPermission = new ProposalPermission();
							proposalPermission.setProposalType(proposalType);
							proposalPermission.setEmployeeId(-1);
							proposalPermission.setDepartmentId(-1);
							proposalPermission.setPositionId(-1);
							proposalPermissionSaves.add(proposalPermission);
						}
						// set real value
						for (int i = 0; i < empIds.size(); i++) {
							proposalPermissionSaves.get(i).setEmployeeId(empIds.get(i));
						}
						for (int i = 0; i < depIds.size(); i++) {
							proposalPermissionSaves.get(i).setDepartmentId(depIds.get(i));
						}
						for (int i = 0; i < posIds.size(); i++) {
							proposalPermissionSaves.get(i).setPositionId(posIds.get(i));
						}
						// save list approval step detail new
						for (ProposalPermission proposalPermission : proposalPermissionSaves) {
							if (proposalPermissionRepository.add(proposalPermission) < 0) {
								return ResponseEntity.status(HttpStatus.OK)
										.body(new ResponseObject("ERROR", "Cập nhật loại đề xuất thất bại", ""));
							}
						}

					}
				}

			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Cập nhật loại đề xuất thất bại", ""));
			}
			// Affter update permission => update name
			if (proposalTypeRepository.edit(proposalType) > 0) {
				// Response data
				approvalOptionViews = new ArrayList<>();
				for (ProposalPermission proposalPermission : proposalPermissionSaves) {
					// have list of all emp or dep or position in all step of proposal
					ApprovalOption_View approvalOptionEmp = approvalOptionReposiroty
							.findById(proposalPermission.getEmployeeId(), "employees");
					ApprovalOption_View approvalOptionDep = approvalOptionReposiroty
							.findById(proposalPermission.getDepartmentId(), "departments");
					ApprovalOption_View approvalOptionPos = approvalOptionReposiroty
							.findById(proposalPermission.getPositionId(), "positions");
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
//				ApprovalStepModel approvalStepModel = new ApprovalStepModel();
//				approvalStepModel.setId(approvalStepEdit.getId());
//				approvalStepModel.setIndex(approvalStepEdit.getApprovalStepIndex());
//				approvalStepModel.setName(approvalStepEdit.getApprovalStepName());
//				approvalStepModel.setApprovalConfigTargets(approvalOptionViews);
				ProposalTypeModel proposalTypeModel = new ProposalTypeModel();
				proposalTypeModel.setId(proposalTypeId);
				proposalTypeModel.setName(proposalTypeName);
				proposalTypeModel.setProposalConfigTargets(approvalOptionViews);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Cập nhật loại để xuất thành công", proposalTypeModel));
			}
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Cập nhật loại để xuất thất bại", ""));
		} catch (Exception e) {
			LOGGER.error(e.getStackTrace().toString());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Cập nhật loại để xuất thất bại", ""));
		}
	}
}
