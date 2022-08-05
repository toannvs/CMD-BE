package com.comaymanagement.cmd.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.constant.CMDConstrant;
import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Pagination;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.RoleDetail;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.RoleDetailModel;
import com.comaymanagement.cmd.model.RoleModel;
import com.comaymanagement.cmd.repositoryimpl.PositionRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.RoleDetailRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.RoleRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class RoleService {
	@Autowired
	RoleRepositoryImpl roleRepository;
	
	@Autowired
	RoleDetailRepositoryImpl roleDetailRepository;
	
	@Autowired
	PositionRepositoryImpl positionRepository;
	
	@Autowired
	Message message;
	
	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	public ResponseEntity<Object> findAll(String name, String sort, String order, String page) {
		page = page == null ? "1" : page.trim();
		name = name == null ? "" : name.trim();
		int limit = CMDConstrant.ROLELIMIT;
		// Caculator offset
		int offset = (Integer.parseInt(page) - 1) * limit;

		// Order by defaut
		if (sort == null || sort == "") {
			sort = "r.id";
		}
		if (order == null || order == "") {
			order = "desc";
		}
		try {
			List<RoleModel> roleModelList = roleRepository.findAll(name, sort, order , limit, offset);
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(roleRepository.countAllPaging(name, sort, order , limit, offset));
			Map<String, Object> results = new TreeMap<String, Object>();
			results.put("roles", roleModelList);
			results.put("pagination", pagination);
			if(roleModelList == null) {
				LOGGER.info("NOT FOUND");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject("Have error:","NOT FOUND",""));
			}else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK","Query produce successfully:",results));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject("ERROR","Have error: ",e.getMessage()));
		}

	}
	// find all
	public ResponseEntity<Object> findRoleDetailByRoleId(Integer roleId){
		RoleDetailModel roleDetailModel =  roleRepository.findRoleDetailByRoleId(roleId);
		if(roleDetailModel == null) {
			LOGGER.info("NOT FOUND");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject("Have error:","NOT FOUND",""));
		}else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK","Query produce successfully:",roleDetailModel));
		}
	}
	
	// add
	public ResponseEntity<Object> add(String json){
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectRole;
		JsonNode jsonObjectOption;
		JsonNode jsonObjectPermission;
		List<RoleDetail> roleDetails = new ArrayList<>();
		Role role = new Role();
		try {
			jsonObjectRole = jsonMapper.readTree(json);
			jsonObjectOption = jsonObjectRole.get("options");
			
			String roleName = jsonObjectRole.get("name").asText();
			Integer createBy = jsonObjectRole.get("createBy").asInt();
			Integer modifyBy = jsonObjectRole.get("modifyBy").asInt();
			String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			
			role.setName(roleName);
			Integer idAdded = roleRepository.add(role);
			Role roleAdded = roleRepository.findById(idAdded);
			for(JsonNode optionNode : jsonObjectOption) {
				for(JsonNode permissionNode : optionNode.get("permissions")) {
					if(permissionNode.get("selected").asBoolean() == true) {
						RoleDetail roleDetail = new RoleDetail();
						roleDetail.setOptionId(optionNode.get("id").asInt());
						roleDetail.setPermissionId(permissionNode.get("id").asInt());
						roleDetail.setCreateBy(createBy);
						roleDetail.setModifyBy(createBy);
						roleDetail.setCreateDate(createDate);
						roleDetail.setModifyDate(createDate);
						roleDetail.setRoleId(idAdded);
						Integer rdAddedId  = roleDetailRepository.add(roleDetail);
					}
				}
			}
			if (idAdded != -1) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", message.getMessageByItemCode("ROLES1"), role));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Error",  message.getMessageByItemCode("ROLEE1") , role));

			}
		} catch (Exception e) {
			LOGGER.error("Have error at add(): ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseObject("Error", e.getMessage(), ""));
		}
		
	}
	
	// edit
	public ResponseEntity<Object> edit(String json){
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectRole;
		JsonNode jsonObjectOption;
		JsonNode jsonObjectPermission;
		List<RoleDetail> roleDetails = new ArrayList<>();
		Role role = new Role();
		try {
			jsonObjectRole = jsonMapper.readTree(json);
			jsonObjectOption = jsonObjectRole.get("options");
			
			Integer roleId = jsonObjectRole.get("id").asInt();
			String roleName = jsonObjectRole.get("name").asText();
			Integer modifyBy = userDetail.getId();
			String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			
			role.setId(roleId);
			role.setName(roleName);
			Integer editSatatus = roleRepository.edit(role);
			roleDetails = roleDetailRepository.findAllByRoleId(roleId);
			for(RoleDetail rd : roleDetails) {
				roleDetailRepository.delete(rd.getId());
			}
			for(JsonNode optionNode : jsonObjectOption) {
				for(JsonNode permissionNode : optionNode.get("permissions")) {
					if(permissionNode.get("selected").asBoolean() == true) {
						RoleDetail roleDetail = new RoleDetail();
						roleDetail.setOptionId(optionNode.get("id").asInt());
						roleDetail.setPermissionId(permissionNode.get("id").asInt());
						roleDetail.setModifyBy(modifyBy);
						roleDetail.setCreateDate(createDate);
						roleDetail.setModifyDate(createDate);
						roleDetail.setRoleId(roleId);
						Integer rdAddedId  = roleDetailRepository.add(roleDetail);
					}
				}
			}
			if (editSatatus != -1) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", message.getMessageByItemCode("ROLES3"), role));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Error",  message.getMessageByItemCode("ROLEE4") , role));
				
			}
		} catch (Exception e) {
			LOGGER.error("Have error at edit(): ", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseObject("Error", e.getMessage(), ""));
		}
		
	}
	
	public ResponseEntity<Object> delete(Integer id){
		List<PositionModel> positions = positionRepository.findAllByRoleId(id);
		if(positions!=null && positions.size()>0) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR",  message.getMessageByItemCode("ROLEE2") , ""));
		}
		try {
			List<RoleDetail> roleDetailList = roleDetailRepository.findAllByRoleId(id);
			for(RoleDetail rd : roleDetailList) {
				roleDetailRepository.delete(rd.getId());
			}
			Integer roleDeleteStatus = roleRepository.delete(id);
			if(roleDeleteStatus != -1 ) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK",  message.getMessageByItemCode("ROLES2") , id));
			}else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("Error",  message.getMessageByItemCode("ROLEE3") , id));
			}
		} catch (Exception e) {
			LOGGER.error("Have error at delete(): ",e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR",  e.getMessage() , ""));
		}
		
	}
	
}
