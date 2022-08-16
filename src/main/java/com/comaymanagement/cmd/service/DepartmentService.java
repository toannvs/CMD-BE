package com.comaymanagement.cmd.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.DepartmentHasDevice;
import com.comaymanagement.cmd.entity.Device;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.model.DeviceModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.repositoryimpl.DepartmentHasDeviceRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.DepartmentRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.PositionRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class DepartmentService {
	@Autowired
	DepartmentRepositoryImpl departmentRepository;
	@Autowired
	PositionRepositoryImpl positionRepository;
	@Autowired
	DepartmentHasDeviceRepositoryImpl departmentHasDeviceRepository;
	@Autowired
	Message message;
	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentService.class);

	public ResponseEntity<Object> findAll(String name) {
		Set<DepartmentModel> departmentModelSet = new LinkedHashSet<>();
		try {
			name = name == null ? "" : name.trim();
			departmentModelSet = departmentRepository.findAll(name);
			if (!name.equals("")) {
				Set<DepartmentModel> departmentModelSetResults = new LinkedHashSet<DepartmentModel>();
				for (DepartmentModel departmentModel : departmentModelSet) {
					if (departmentModel.getFatherDepartmentId() != -1) {
						Boolean loop = true;
						int idTemp = departmentModel.getFatherDepartmentId();
						while (loop) {
							Department departmentTemp = departmentRepository.findById(idTemp);
							DepartmentModel departmentModelTemp = departmentRepository.toModel(departmentTemp);
							Boolean addFlag = true;
							for (DepartmentModel item : departmentModelSetResults) {
								if (item.getId() == departmentModelTemp.getId()) {
									addFlag = false;
								}
							}
							if (addFlag) {
								departmentModelSetResults.add(departmentModelTemp);
							}
							if (departmentModelTemp.getFatherDepartmentId() != -1) {
								idTemp = departmentModelTemp.getFatherDepartmentId();
							} else {
								loop = false;
							}
						}
					}
					departmentModelSetResults.add(departmentModel);
				}
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "", departmentModelSetResults));
			}

			if (departmentModelSet.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", departmentModelSet));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Có lỗi xảy ra", departmentModelSet));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), departmentModelSet));
		}

	}

	public ResponseEntity<Object> add(String json) {
		List<Position> positionList = new ArrayList<>();
		Department dep = new Department();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectDepartment;
		JsonNode jsonObjectPosition;
		try {
			jsonObjectDepartment = jsonMapper.readTree(json);
			jsonObjectPosition = jsonObjectDepartment.get("positions");
			// Get data
			String code = jsonObjectDepartment.get("code").asText();
			if (code.length() > 10) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("DEPE5"), ""));
			}
			String name = jsonObjectDepartment.get("name") != null ? jsonObjectDepartment.get("name").asText() : "";
			Integer fatherDepartmentId = jsonObjectDepartment.get("fatherDepartmentId") != null
					? jsonObjectDepartment.get("fatherDepartmentId").asInt()
					: -1;
			fatherDepartmentId = fatherDepartmentId == 0 ? -1 : fatherDepartmentId;
			String description = jsonObjectDepartment.get("description") != null
					? jsonObjectDepartment.get("description").asText()
					: "";
			Integer createBy = jsonObjectDepartment.get("createBy") != null
					? jsonObjectDepartment.get("createBy").asInt()
					: -1;
			String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			Integer modifyBy = -1;
			String modifyDate = "";
			Integer level = jsonObjectDepartment.get("level") != null ? jsonObjectDepartment.get("level").asInt() : -1;
			Integer headPosition = -1;

//			Check department code existed
			boolean isExisted = departmentRepository.isExisted(-1, code);

			if (isExisted) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("DEPE2"), ""));
			}

			dep.setCode(code);
			dep.setName(name);
			dep.setFatherDepartmentId(fatherDepartmentId);
			dep.setDescription(description);
			dep.setCreateBy(createBy);
			dep.setCreateDate(createDate);
			dep.setModifyBy(modifyBy);
			dep.setModifyDate(modifyDate);
			dep.setLevel(level);
			dep.setHeadPosition(headPosition);
			// save department..............
			Integer idDepAdded = departmentRepository.add(dep);
			if (idDepAdded == -1) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("DEPE3"), ""));
			}
			Department depUpdate = departmentRepository.findById(idDepAdded);
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
				pos.setDepartment(dep);
				positionList.add(pos);
			}
			dep.setPositions(positionList);
			for (Position p : positionList) {

				if (positionRepository.add(p) < 0) {
					LOGGER.error("Error has occured in DepartmentService at save():");
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", message.getMessageByItemCode("POSE1"), ""));
				}
				if (p.getIsManager()) {
					depUpdate.setHeadPosition(p.getId());
					dep.setHeadPosition(p.getId());
					departmentRepository.edit(depUpdate);
				}
			}

			if (idDepAdded != -1) {
				DepartmentModel departmentModel = toDepartmentModel(dep);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("DEPS1"), departmentModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("DEPE3"), dep));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentService at add() ", e);
			LOGGER.error(json);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));

		}
	}

	public ResponseEntity<Object> edit(String json) {
		List<Position> positionEdits = new ArrayList<>();
		List<Position> positionAdds = new ArrayList<>();
		Department dep = null;
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectDepartment;
		JsonNode jsonObjectPosition;

		try {
			jsonObjectDepartment = jsonMapper.readTree(json);
			jsonObjectPosition = jsonObjectDepartment.get("positions");
			// Get data
			String code = jsonObjectDepartment.get("code").asText();
			String name = jsonObjectDepartment.get("name") != null ? jsonObjectDepartment.get("name").asText() : "";
			Integer fatherDepartmentId = jsonObjectDepartment.get("fatherDepartmentId") != null
					? jsonObjectDepartment.get("fatherDepartmentId").asInt()
					: -1;
			String description = jsonObjectDepartment.get("description") != null
					? jsonObjectDepartment.get("description").asText()
					: "";
			Integer createBy = jsonObjectDepartment.get("createBy") != null
					? jsonObjectDepartment.get("createBy").asInt()
					: -1;
			String createDate = jsonObjectDepartment.get("createDate") != null
					? jsonObjectDepartment.get("createDate").asText()
					: "";
			Integer modifyBy = jsonObjectDepartment.get("modifyBy") != null
					? jsonObjectDepartment.get("modifyBy").asInt()
					: -1;
			String modifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			Integer level = jsonObjectDepartment.get("level") != null ? jsonObjectDepartment.get("level").asInt() : -1;
			Integer headPosition = -1;
//			Check department code existed
			Integer id = jsonObjectDepartment.get("id").asInt();
			dep = departmentRepository.findById(id);
			boolean isExisted = departmentRepository.isExisted(id, code);
			if (isExisted) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("DEPE2"), ""));
			}
			dep.setCode(code);
			dep.setName(name);
			dep.setFatherDepartmentId(fatherDepartmentId);
			dep.setDescription(description);
			dep.setCreateDate(createDate);
			dep.setCreateBy(createBy);
			dep.setModifyBy(modifyBy);
			dep.setModifyDate(modifyDate);
			dep.setLevel(level);
			dep.setHeadPosition(headPosition);

			// save department..............
			Integer messageEdit = departmentRepository.edit(dep);
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
					pos.setDepartment(dep);
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
					pos.setDepartment(dep);
					pos.setCreateBy(createBy);
					pos.setModifyBy(modifyBy);
					pos.setCreateDate(createDate);
					pos.setModifyDate(modifyDate);
					positionAdds.add(pos);
				}

			}
			dep.setPositions(new ArrayList<Position>());
			// Add position
			for (Position p : positionEdits) {
				if (positionRepository.edit(p) < 0) {
					LOGGER.error("Error has occured in DepartmentService at edit():");
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", message.getMessageByItemCode("POSE2"), ""));
				} else if (p.getIsManager()) {
					dep.setHeadPosition(p.getId());
					departmentRepository.edit(dep);
				}
				dep.getPositions().add(p);
			}
			// Edit position
			for (Position p : positionAdds) {

				if (positionRepository.add(p) < 0) {
					LOGGER.error("Error has occured in DepartmentService at edit():");
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", message.getMessageByItemCode("POSE1"), ""));
				} else if (p.getIsManager()) {

					dep.setHeadPosition(p.getId());
					departmentRepository.edit(dep);
				}
				dep.getPositions().add(p);
			}
			if (messageEdit != -1) {
				DepartmentModel departmentModel = toDepartmentModel(dep);
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", message.getMessageByItemCode("DEPS2"), departmentModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("DEPE4"), dep));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentService at add() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("Error", e.getMessage(), ""));

		}
	}

	public ResponseEntity<Object> delete(Integer id) {
		Department depDelete = (Department) departmentRepository.findById(id);
		if (depDelete.getEmployees().size() > 0) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", message.getMessageByItemCode("DEPE1"), ""));
		}
		for (Position p : depDelete.getPositions()) {
			positionRepository.delete(p.getId());
		}
		String deleteStatus = departmentRepository.delete(id);
		try {
			if (deleteStatus.equals("1")) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", deleteStatus + "", ""));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", deleteStatus, ""));

			}
		} catch (Exception e) {
			LOGGER.error("Has error: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public DepartmentModel toDepartmentModel(Department d) {
		try {
			DepartmentModel departmentModel = new DepartmentModel();
			List<PositionModel> positionModelList = new ArrayList<>();
			departmentModel.setId(d.getId());
			departmentModel.setCode(d.getCode());
			departmentModel.setName(d.getName());
			departmentModel.setDescription(d.getDescription());
			departmentModel.setFatherDepartmentId(d.getFatherDepartmentId());
			departmentModel.setLevel(d.getLevel());
			for (Position pos : d.getPositions()) {
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
			departmentModel.setPositions(positionModelList);
			departmentModel.setHeadPosition(d.getHeadPosition());
			return departmentModel;
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at findAll() ", e);
			return null;
		}
	}

	public ResponseEntity<Object> findById(Integer id) {
		try {
			Department dep = departmentRepository.findById(id);
			DepartmentModel departmentModel = departmentRepository.toModel(dep);
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", departmentModel));
		} catch (Exception e) {
			LOGGER.error("Error has occured at findById() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("OK", "Not found", ""));
		}
	}

	public ResponseEntity<Object> findAllDeviceByDepartmentName(String name) {
		List<DepartmentModel> depModelResults = new ArrayList<>();
		try {
			Set<Department> departmentWithDevices = new LinkedHashSet<>();
			List<DeviceModel> deviceModels;
			departmentWithDevices = departmentHasDeviceRepository.findAllDeviceByDepartmentName(name);
			for (Department dep : departmentWithDevices) {
				List<DepartmentHasDevice> depHasDevices = dep.getDepartmentHasDevices();
				deviceModels = new ArrayList<>();
				for (DepartmentHasDevice departmentHasDevice : depHasDevices) {
					Device device = departmentHasDevice.getDevice();
					DeviceModel deviceModel = new DeviceModel();
					deviceModel.setId(departmentHasDevice.getId());
					deviceModel.setName(device.getName());
					deviceModel.setDescription(departmentHasDevice.getDescription());
					deviceModel.setIsActive(departmentHasDevice.getIsActive());
					deviceModels.add(deviceModel);
				}
				DepartmentModel depModel = departmentRepository.toModel(dep);
				depModel.setDevices(deviceModels);
				depModelResults.add(depModel);
			}
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", depModelResults));
		} catch (Exception e) {
			LOGGER.error("Error has occured at findById() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("OK", "Not found", depModelResults));
		}
	}

	public ResponseEntity<Object> addDeviceForDepartment( String json) {
		JsonNode jsonOject = null;
		JsonMapper jsonMapper = new JsonMapper();
		Integer departmentId = null;
		Integer deviceId = null;
		String description = null;
		Boolean active = false;
		try {
			jsonOject = jsonMapper.readTree(json);
			departmentId = ((null == jsonOject.get("departmentId")) || (jsonOject.get("departmentId").asInt() <= 0)) ? -1 : jsonOject.get("departmentId").asInt();
			Department department = departmentRepository.findById(departmentId);
			if(null == department) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR",message.getMessageByItemCode("DEPE6"), departmentId));
			}
			deviceId = ((null == jsonOject.get("deviceId")) || (jsonOject.get("deviceId").asInt() <= 0)) ? -1 : jsonOject.get("deviceId").asInt();
			Device device = departmentHasDeviceRepository.findById(deviceId);
			if(null == device) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR",message.getMessageByItemCode("DEVE1"), ""));
			}
			description = ((null == jsonOject.get("description")) || (jsonOject.get("description").asText() == "")) ? "" : jsonOject.get("description").asText();
			active = (((null == jsonOject.get("isActive")) || (jsonOject.get("isActive").asInt() <= 0)) ? 0 : jsonOject.get("isActive").asInt()) == 0 ? false : true;
			
			DepartmentHasDevice departmentHasDevice = new DepartmentHasDevice();
			departmentHasDevice.setDevice(device);
			departmentHasDevice.setDepartment(department);
			departmentHasDevice.setDescription(description);
			departmentHasDevice.setIsActive(active);
			
			DeviceModel deviceModel  = departmentHasDeviceRepository.add(departmentHasDevice);
			if(null != deviceModel) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK","", deviceModel));
			}else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR","", ""));
			}
		}catch(Exception e) {
			LOGGER.error("Error has occured at findById() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR","",e.getMessage() ));
		}
	}
	
	public ResponseEntity<Object> editDeviceForDepartment( String json) {
		JsonNode jsonOject = null;
		JsonMapper jsonMapper = new JsonMapper();
		Integer id = null;
		String description = null;
		Boolean active = false;
		try {
			jsonOject = jsonMapper.readTree(json);
			id = ((null == jsonOject.get("id")) || (jsonOject.get("id").asInt() <= 0)) ? -1 : jsonOject.get("id").asInt();
			description = ((null == jsonOject.get("description")) || (jsonOject.get("description").asText() == "")) ? "" : jsonOject.get("description").asText();
			active = (((null == jsonOject.get("isActive")) || (jsonOject.get("isActive").asInt() <= 0)) ? 0 : jsonOject.get("isActive").asInt()) == 0 ? false : true;
			DepartmentHasDevice departmentHasDevice  = departmentHasDeviceRepository.findDepartmentHasDeviceById(id);
			departmentHasDevice.setDescription(description);
			departmentHasDevice.setIsActive(active);
			
			DeviceModel deviceModel = departmentHasDeviceRepository.edit(departmentHasDevice);
			if(null != deviceModel) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK","", deviceModel));
			}else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR","", ""));
			}
		}catch(Exception e) {
			LOGGER.error("Error has occured at findById() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR","",e.getMessage() ));
		}
	}
	
	public ResponseEntity<Object> deleteDeviceForDepartment(Integer id) {
		Boolean result = false;
		try {
			DepartmentHasDevice departmentHasDevice  = departmentHasDeviceRepository.findDepartmentHasDeviceById(id);
			result = departmentHasDeviceRepository.delete(departmentHasDevice);
			if(null != result) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK",message.getMessageByItemCode("DEVS1"), ""));
			}else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR",message.getMessageByItemCode("DEVE2"), ""));
			}
		}catch(Exception e) {
			LOGGER.error("Error has occured at findById() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR","",e.getMessage() ));
		}
	}
}
