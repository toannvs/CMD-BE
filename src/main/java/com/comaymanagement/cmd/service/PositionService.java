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
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.repositoryimpl.PositionRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class PositionService{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	PositionRepositoryImpl positionRepository;
	@Autowired
	Message message;
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	public ResponseEntity<Object> findAllByRoleId(Integer roleId) {
		List<PositionModel> positionModelList = new ArrayList<PositionModel>();;

		try {
			if (roleId == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Have error: ", "Role ID is null"));
			} else {
				positionModelList = positionRepository.findAllByRoleId(roleId);
				if (positionModelList.size() < 1) {
					LOGGER.info("Have no task by status_id: " + roleId);
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("", "Have no task by status_id: " + roleId, ""));
				} else {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("OK", "Query produce SUCCESSFULLY:", positionModelList));
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", "Have error: ", e.getMessage()));
		}
	}
	public ResponseEntity<Object> findAllByDepartmentId(Integer depId) {
		List<PositionModel> positionModelList = new ArrayList<PositionModel>();;

		try {
			if (depId == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Have error: ", "Department ID is null"));
			} else {
				positionModelList = positionRepository.findAllByDepartmentId(depId);
				if (positionModelList.size() < 1) {
					LOGGER.info("Have no task by status_id: " + depId);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseObject("", "Have no position by departmentId: " + depId, ""));
				} else {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("OK", "Query produce SUCCESSFULLY:", positionModelList));
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", "Have error: ", e.getMessage()));
		}
	}
	public ResponseEntity<Object> findAllByTeamId(Integer teamId) {
		List<PositionModel> positionModelList = new ArrayList<PositionModel>();;
		
		try {
			if (teamId == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Have error: ", "Department ID is null"));
			} else {
				positionModelList = positionRepository.findAllByDepartmentId(teamId);
				if (positionModelList.size() < 1) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(new ResponseObject("", "Have no position by teamId: " + teamId, ""));
				} else {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("OK", "Query produce SUCCESSFULLY:", positionModelList));
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseObject("ERROR", "Have error: ", e.getMessage()));
		}
	}

	public ResponseEntity<Object> findAllByDepartmentIds(String json) {
		List<Integer> departmentIds = new ArrayList<>();
		try {
			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObject;
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonDepObject = jsonObject.get("departmentIds");
			for (JsonNode departmendId : jsonDepObject) {
				departmentIds.add(Integer.valueOf(departmendId.toString()));
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		List<PositionModel> positionModelList = new ArrayList<PositionModel>();;
		for(Integer depId : departmentIds) {
			List<PositionModel> positionModelListTMP = new ArrayList<>();
			positionModelListTMP = positionRepository.findAllByDepartmentId(depId);
				if (positionModelListTMP.size() > 0) {
					for(PositionModel depModel : positionModelListTMP) {
						positionModelList.add(depModel);
					}
					
				} else {
					LOGGER.info("Have no task by status_id: " + depId);
				}
		}
		if(positionModelList.size()>0) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "SUCCESSFULLY:", positionModelList));
		}else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Not found", ""));
		}
			
	}
//	public ResponseEntity<Object> add(String json) {
//		JsonMapper jsonMapper = new JsonMapper();
//		JsonNode jsonObjectPosition;
//		Position p = new Position();
//		Department dep = new Department();
//		Role role = new Role();
//		Team team = new Team();
//		Integer id = -1;
//		try {
//			
//			jsonObjectPosition = jsonMapper.readTree(json);
////			String name = jsonObjectPosition.get("name") != null ? jsonObjectPosition.get("name").asText() : "";
////			String name = jsonObjectPosition.get("name") != null ? jsonObjectPosition.get("name").asText() : "";
////			String name = jsonObjectPosition.get("name") != null ? jsonObjectPosition.get("name").asText() : "";
////			String name = jsonObjectPosition.get("name") != null ? jsonObjectPosition.get("name").asText() : "";
////			String name = jsonObjectPosition.get("name") != null ? jsonObjectPosition.get("name").asText() : "";
////			String name = jsonObjectPosition.get("name") != null ? jsonObjectPosition.get("name").asText() : "";
////			String name = jsonObjectPosition.get("name") != null ? jsonObjectPosition.get("name").asText() : "";
////			String name = jsonObjectPosition.get("name") != null ? jsonObjectPosition.get("name").asText() : "";
////			
//			
//			p.setName(jsonObjectPosition.get("name").asText());
//			p.setIsManager(jsonObjectPosition.get("isManager").asBoolean());
//			team.setId(jsonObjectPosition.get("teamId").asInt());
//			dep.setId(jsonObjectPosition.get("departmentId").asInt());
//			role.setId(jsonObjectPosition.get("roleId").asInt());
//			p.setTeam(team);
//			p.setDepartment(dep);
//			p.setRole(role);
//			if (positionRepository.add(p)>0) {
//				PositionModel positionModel = positionRepository.toModel(p);
//				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK","", positionModel));
//			} else {
//				return ResponseEntity.status(HttpStatus.OK)
//						.body(new ResponseObject("Error", "", p));
//			}
//		} catch (Exception e) {
//			logger.error("Error has occured in PositionService at save()", e);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("Error", e.getMessage(), ""));
//		}
//	}
	public ResponseEntity<Object> delete(String id) {
		try {
			String updateStatus = positionRepository.delete(Integer.valueOf(id));

			if (updateStatus.equals("1")) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", updateStatus + "", ""));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Error", updateStatus + "", ""));

			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("Error", "", ""));

		}

	}
	public ResponseEntity<Object> add(Position p) {
		if ( positionRepository.add(p)>0) {
			PositionModel positionModel = positionRepository.toModel(p);
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK",message.getMessageByItemCode("POSS1"), positionModel));
		} else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("Error",message.getMessageByItemCode("POSE1"), p));
		}
	}
	

}
