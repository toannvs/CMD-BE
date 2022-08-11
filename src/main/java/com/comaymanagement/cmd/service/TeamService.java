package com.comaymanagement.cmd.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.Team;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.TeamModel;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.PositionRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.TeamRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class TeamService {
	@Autowired
	TeamRepositoryImpl teamRepository;
	@Autowired
	PositionRepositoryImpl positionRepository;

	@Autowired
	Message message;
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);

	public ResponseEntity<Object> findAll(String name) {
		name = name == null ? "" : name.trim();
		Set<TeamModel> teamModelSet = teamRepository.findAll(name);

		if (teamModelSet!=null) {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "SUCCESSFULLY", teamModelSet));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Có lỗi xảy ra",teamModelSet ));
		}

	}

	public ResponseEntity<Object> add(String json) {
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		List<Position> positionList = new ArrayList<>();
		Team team = new Team();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectTeam;
		JsonNode jsonObjectPosition;
		try {
			jsonObjectTeam = jsonMapper.readTree(json);
			jsonObjectPosition = jsonObjectTeam.get("positions");
			// Get data
			String code = jsonObjectTeam.get("code").asText();
			if (code.length() > 10) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TEAME4"), ""));
			}
			String name = jsonObjectTeam.get("name") != null ? jsonObjectTeam.get("name").asText() : "";
			String description = jsonObjectTeam.get("description") != null ? jsonObjectTeam.get("description").asText()
					: "";
			Integer createBy = userDetail.getId();
			String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			Integer modifyBy = userDetail.getId();
			String modifyDate = "";
			Integer headPosition = -1;

//			Check team code existed
			boolean isExisted = teamRepository.isExisted(-1, code);

			if (isExisted) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TEAME1"), ""));
			}

			team.setCode(code);
			team.setName(name);
			team.setDescription(description);
			team.setCreateBy(createBy);
			team.setCreateDate(createDate);
			team.setModifyBy(modifyBy);
			team.setModifyDate(modifyDate);
			team.setHeadPosition(headPosition);
			// save team..............
			Integer idTeamAdded = teamRepository.add(team);
			if (idTeamAdded == -1) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TEAME2"), ""));
			}
			Team teamUpdate = teamRepository.findById(idTeamAdded);
			for (JsonNode p : jsonObjectPosition) {
				Role role = new Role();
				Position pos = new Position();
				role.setId(p.get("role").get("id").asInt());
				pos.setName(p.get("name").asText());
				pos.setIsManager(p.get("isManager").asBoolean());
				pos.setCreateBy(createBy);
				pos.setModifyBy(modifyBy);
				pos.setCreateDate(createDate);
				pos.setModifyDate(modifyDate);
				pos.setRole(role);
				pos.setTeam(team);
				positionList.add(pos);
			}
			team.setPositions(positionList);
			for (Position p : positionList) {
				if (positionRepository.add(p)<0) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", message.getMessageByItemCode("POSE1"), ""));
				}
				if (p.getIsManager()) {
					teamUpdate.setHeadPosition(p.getId());
					team.setHeadPosition(p.getId());
					teamRepository.edit(teamUpdate);
				}
			}

			if (idTeamAdded != -1) {
				TeamModel teamModel = toTeamModel(team);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("TEAMS4"), teamModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TEAME2"), team));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured at add() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));

		}
	}

	public ResponseEntity<Object> edit(String json) {
		List<Position> positionEdits = new ArrayList<>();
		List<Position> positionAdds = new ArrayList<>();
		Team team = new Team();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectTeam;
		JsonNode jsonObjectPosition;

		try {
			jsonObjectTeam = jsonMapper.readTree(json);
			jsonObjectPosition = jsonObjectTeam.get("positions");
			// Get data
			String code = jsonObjectTeam.get("code").asText();
			String name = jsonObjectTeam.get("name") != null ? jsonObjectTeam.get("name").asText() : "";
			String description = jsonObjectTeam.get("description") != null ? jsonObjectTeam.get("description").asText()
					: "";
			Integer createBy = jsonObjectTeam.get("createBy") != null ? jsonObjectTeam.get("createBy").asInt() : -1;
			String createDate = jsonObjectTeam.get("createDate") != null ? jsonObjectTeam.get("createDate").asText()
					: "";
			Integer modifyBy = jsonObjectTeam.get("modifyBy") != null ? jsonObjectTeam.get("modifyBy").asInt() : -1;
			String modifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			Integer headPosition = -1;
//			Check department code existed
			Integer id = jsonObjectTeam.get("id").asInt();
			team = teamRepository.findById(id);
			boolean isExisted = teamRepository.isExisted(id, code);
			if (isExisted) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("TEAME3"), ""));
			}
			team.setCode(code);
			team.setName(name);
			team.setDescription(description);
			team.setCreateBy(createBy);
			team.setCreateDate(createDate);
			team.setModifyBy(modifyBy);
			team.setModifyDate(modifyDate);
			team.setHeadPosition(headPosition);
			// save department..............
			Integer messageEdit = teamRepository.edit(team);
			for (JsonNode p : jsonObjectPosition) {
				Role role = new Role();
				Position pos = new Position();
				// If don't have id => go to save, else => go to edit
				Integer posId = p.get("id") != null ? p.get("id").asInt() : -1;
				if (posId != -1) {
					role.setId(p.get("role").get("id").asInt());
					pos.setId(posId);
					pos.setName(p.get("name").asText());
					pos.setIsManager(p.get("isManager").asBoolean());
					pos.setRole(role);
					pos.setTeam(team);
					pos.setCreateBy(createBy);
					pos.setModifyBy(modifyBy);
					pos.setCreateDate(createDate);
					pos.setModifyDate(modifyDate);
					positionEdits.add(pos);
				} else {

					role.setId(p.get("role").get("id").asInt());
					pos.setName(p.get("name").asText());
					pos.setIsManager(p.get("isManager").asBoolean());
					pos.setRole(role);
					pos.setTeam(team);
					pos.setCreateBy(createBy);
					pos.setModifyBy(modifyBy);
					pos.setCreateDate(createDate);
					pos.setModifyDate(modifyDate);
					positionAdds.add(pos);
				}

			}
			// Add position
			team.setPositions(new ArrayList<Position>());
			for (Position p : positionAdds) {
				if (positionRepository.add(p)<0) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("Error", message.getMessageByItemCode("POSE1"), ""));
				} else if (p.getIsManager()) {
					team.setHeadPosition(p.getId());
					teamRepository.edit(team);
				}
				team.getPositions().add(p);
			}
			// Edit position
			for (Position p : positionEdits) {
				if (positionRepository.edit(p)<0) {
					LOGGER.error("Error has occured at edit():");
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", message.getMessageByItemCode("POSE2"), ""));
				} else if (p.getIsManager()) {
					team.setHeadPosition(p.getId());
					teamRepository.edit(team);
				}
				team.getPositions().add(p);
			}

			if (messageEdit != -1) {
				TeamModel teamModel = toTeamModel(team);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("TEAMS5"), teamModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Error", "", team));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured at add() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("Error", e.getMessage(), ""));

		}
	}

	public ResponseEntity<Object> delete(Integer id) {
		Team teamDelete = teamRepository.findById(id);
		if (teamDelete.getEmployees().size() > 0) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", message.getMessageByItemCode("TEAME2"), ""));
		}
		String deleteStatus = teamRepository.delete(id);
		try {
			if (deleteStatus.equals("1")) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("TEAMS6"), ""));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", deleteStatus + "", ""));

			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}
	public TeamModel toTeamModel(Team t) {
		try {
				TeamModel toTeamModel = new TeamModel();
				List<PositionModel> positionModelList = new ArrayList<>();
				toTeamModel.setId(t.getId());
				toTeamModel.setCode(t.getCode());
				toTeamModel.setName(t.getName());
				toTeamModel.setDescription(t.getDescription());
				for (Position pos : t.getPositions()) {
					PositionModel positionModel = new PositionModel();
					Role role = new Role();
					role.setId(pos.getRole().getId());
					role.setName(pos.getRole().getName());
					positionModel.setId(pos.getId());
					positionModel.setName(pos.getName());
					positionModel.setIsManager(pos.getIsManager());
					positionModel.setRole(role);
					positionModelList.add(positionModel);
				}
				toTeamModel.setPositions(positionModelList);
				toTeamModel.setHeadPosition(t.getHeadPosition());
				return toTeamModel;
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at findAll() ", e);
			return null;
		}
	}
	public ResponseEntity<Object> findById (Integer id) {
		try {
			Team team = teamRepository.findById(id);
			TeamModel teamModel = teamRepository.toModel(team);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK","", teamModel));
		} catch (Exception e) {
			LOGGER.error("Error has occured at findById() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR","Not found", ""));
		}
	}
}
