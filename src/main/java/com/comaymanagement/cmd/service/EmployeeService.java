package com.comaymanagement.cmd.service;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.comaymanagement.cmd.constant.CMDConstrant;
import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Pagination;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.Team;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.model.EmployeeModel;
import com.comaymanagement.cmd.model.NotifyModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.TeamModel;
import com.comaymanagement.cmd.model.UserModel;
import com.comaymanagement.cmd.repositoryimpl.DepartmentRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.NotifyRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.PositionRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.TeamRepositoryImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

@Service
@Transactional(rollbackFor = Exception.class)
public class EmployeeService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	EmployeeRepositoryImpl employeeRepository;

	@Autowired
	DepartmentRepositoryImpl departmentRepository;

	@Autowired
	PositionRepositoryImpl positionRepository;

	@Autowired
	TeamRepositoryImpl teamRepository;

	@Autowired
	Message message;
	
	@Autowired
	PasswordEncoder encoder;
	
	@Autowired
	NotifyRepositoryImpl notifyRepositoryImpl;

	@Autowired
	CustomRoleService customRoleService;
	static int countFile = 0;
	public ResponseEntity<Object> findAllWithParamAndNotLimit(String page, 
			String sort, String order, String json) {
		Integer limit = -1;
	
		Set<EmployeeModel> employeeModelSetTMP = new LinkedHashSet<>();
		Set<EmployeeModel> employeeModelSet = new LinkedHashSet<>();
		List<Integer> departmentIds = new ArrayList<Integer>();
		List<Integer> positionIds = new ArrayList<Integer>();
		String name = "";
		String dob = "";
		String email = "";
		String phone = "";
		
//		name = name == null ? "" : name.trim();
		try {
			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObject;
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonDepObject = jsonObject.get("departmentIds");
			JsonNode jsonPosObject = jsonObject.get("positionIds");
			
			name = jsonObject.get("name") == null ? ""
					: jsonObject.get("name").asText() == "null" ? ""
							: jsonObject.get("name").asText();
			dob = jsonObject.get("dob") == null ? ""
					: jsonObject.get("dob").asText() == "null" ? ""
							: jsonObject.get("dob").asText();
			email = jsonObject.get("email") == null ? ""
					: jsonObject.get("email").asText() == "null" ? ""
							: jsonObject.get("email").asText();
			phone = jsonObject.get("phone") == null ? ""
					: jsonObject.get("phone").asText() == "null" ? ""
							: jsonObject.get("phone").asText();
			for (JsonNode departmendId : jsonDepObject) {
				departmentIds.add(Integer.valueOf(departmendId.toString()));
			}
			for (JsonNode positionsId : jsonPosObject) {
				positionIds.add(Integer.valueOf(positionsId.toString()));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		
		page = page == null ? "1" : page.trim();
		// Order by defaut
		if (sort == null || sort == "") {
			sort = "emp.id";
		}
		if (order == null || order == "") {
			order = "desc";
		}
		int count = Integer.parseInt(page);
		int offset = 0;
		int limitCaculated = 0;
		Integer totalItemEmployeeDup = employeeRepository.countAllPagingIncludeDuplicate(name, dob, email, phone, departmentIds, positionIds, sort, order,-1,-1);
		Integer totalItemEmployee = employeeRepository.countAllPaging(name, dob, email, phone, departmentIds, positionIds, sort, order,-1,-1);
		Map<String, Integer> caculatorOffset = new LinkedHashMap<>();
		while(count>0) {
			if((offset + limit) > totalItemEmployeeDup) {
				limit = employeeRepository.countAllPaging(name, dob, email, phone, departmentIds, positionIds, sort, order,offset	,-1);;
			}
			caculatorOffset = caculatorOffset(name, dob, email, phone, departmentIds, positionIds, sort, order, limit,offset);
			if(count>1) {
				offset = caculatorOffset.get("offset");
			}
			limitCaculated = caculatorOffset.get("limit");
			count--;
		}
		try {
			
			// Get with limit = -1
			employeeModelSetTMP = employeeRepository.findAll(name, dob, email, phone, departmentIds, positionIds, sort, order, limit,
					offset);
			for (EmployeeModel employeeModel : employeeModelSetTMP) {
				employeeModelSet.add(employeeModel);
			}
			Pagination pagination = new Pagination();
			Map<String, Object> result = new TreeMap<>();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(totalItemEmployee);

			result.put("pagination", pagination);
			result.put("employees", employeeModelSet);
			if (employeeModelSet.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", result));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Not found", result));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in employeePaging() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", e.getMessage(), ""));
		}

	}
	
	// Find all employee and search
	public ResponseEntity<Object> employeePaging(String page, 
			String sort, String order, String json) {
		Integer limit = CMDConstrant.LIMIT;
	
		Set<EmployeeModel> employeeModelSetTMP = new LinkedHashSet<>();
		Set<EmployeeModel> employeeModelSet = new LinkedHashSet<>();
		List<Integer> departmentIds = new ArrayList<Integer>();
		List<Integer> positionIds = new ArrayList<Integer>();
		String name = "";
		String dob = "";
		String email = "";
		String phone = "";
		
//		name = name == null ? "" : name.trim();
		try {
			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObject;
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonDepObject = jsonObject.get("departmentIds");
			JsonNode jsonPosObject = jsonObject.get("positionIds");
			
			name = jsonObject.get("name") == null ? ""
					: jsonObject.get("name").asText() == "null" ? ""
							: jsonObject.get("name").asText();
			dob = jsonObject.get("dob") == null ? ""
					: jsonObject.get("dob").asText() == "null" ? ""
							: jsonObject.get("dob").asText();
			email = jsonObject.get("email") == null ? ""
					: jsonObject.get("email").asText() == "null" ? ""
							: jsonObject.get("email").asText();
			phone = jsonObject.get("phone") == null ? ""
					: jsonObject.get("phone").asText() == "null" ? ""
							: jsonObject.get("phone").asText();
			for (JsonNode departmendId : jsonDepObject) {
				departmentIds.add(Integer.valueOf(departmendId.toString()));
			}
			for (JsonNode positionsId : jsonPosObject) {
				positionIds.add(Integer.valueOf(positionsId.toString()));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		
		page = page == null ? "1" : page.trim();
		// Order by defaut
		if (sort == null || sort == "") {
			sort = "emp.id";
		}
		if (order == null || order == "") {
			order = "desc";
		}
		int count = Integer.parseInt(page);
		int offset = 0;
		int limitCaculated = 0;
		Integer totalItemEmployeeDup = employeeRepository.countAllPagingIncludeDuplicate(name, dob, email, phone, departmentIds, positionIds, sort, order,-1,-1);
		Integer totalItemEmployee = employeeRepository.countAllPaging(name, dob, email, phone, departmentIds, positionIds, sort, order,-1,-1);
		Map<String, Integer> caculatorOffset = new LinkedHashMap<>();
		while(count>0) {
			if((offset + limit) > totalItemEmployeeDup) {
				limit = employeeRepository.countAllPaging(name, dob, email, phone, departmentIds, positionIds, sort, order,offset	,-1);;
			}
			caculatorOffset = caculatorOffset(name, dob, email, phone, departmentIds, positionIds, sort, order, limit,offset);
			if(count>1) {
				offset = caculatorOffset.get("offset");
			}
			limitCaculated = caculatorOffset.get("limit");
			count--;	
		}
		try {
			// if duplicate => limit will alway be >= CMDConstrant.LIMIT
			if(limitCaculated < 15) {
				employeeModelSetTMP = employeeRepository.findAll(name, dob, email, phone, departmentIds, positionIds, sort, order, CMDConstrant.LIMIT,
						offset);
			}else {
				employeeModelSetTMP = employeeRepository.findAll(name, dob, email, phone, departmentIds, positionIds, sort, order, limitCaculated,
						offset);
			}

			for (EmployeeModel employeeModel : employeeModelSetTMP) {
				employeeModelSet.add(employeeModel);
			}
			Pagination pagination = new Pagination();
			Map<String, Object> result = new TreeMap<>();
			pagination.setLimit(CMDConstrant.LIMIT);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(totalItemEmployee);

			result.put("pagination", pagination);
			result.put("employees", employeeModelSet);
			if (employeeModelSet.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", result));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Not found", result));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in employeePaging() ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", e.getMessage(), ""));
		}

	}
	public Map<String, Integer> caculatorOffset(String name, String dob, String email, String phone,
			List<Integer> departmentIds,
			List<Integer> positionIds, String sort, String order,Integer limit, Integer offset) {
		int quantityDifference =0;
		int newOffset = offset;
		int newLimit = limit;
		int total = employeeRepository.countAllPaging(name, dob, email, phone, departmentIds, positionIds, sort, order, -1, -1); //10, 3
		Map<String, Integer> result = new LinkedHashMap<>();
			do {
				
				int countPaging = employeeRepository.countAllPaging(name, dob, email, phone, departmentIds, positionIds, sort, order, newOffset, newLimit); //10, 3
				quantityDifference = newLimit - countPaging; // , , 0
				//store old offset
//				newOffset = offset;
				newLimit = quantityDifference; //5 //2 //0
				//expect offset
				newOffset = newOffset + countPaging + quantityDifference; // 15 // 15+3+2 // 20+2+0
				limit = limit + newLimit; // 15 +5 , 20 + 2 //22+0
//				offset = newOffset ;
			} while (quantityDifference>0 );
				result.put("offset", newOffset); //22+0
				result.put("limit", limit); //22+0
			
			
//		if(count>0) {
//			caculatorOffset((count-1) +"" ,name,  dob,  email,  phone, dep,  pos,  sort,  order, limit);
//		}
	return result;
	}
	// Add and edit employee
	public ResponseEntity<Object> addEmployee(String json) {
		Employee emp = new Employee();
		UserModel user = new UserModel();
		String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
		String modifyDate = createDate;
		List<Position> positionList = new ArrayList<>();
		List<Team> teamList = new ArrayList<>();
		List<Department> departmentList = new ArrayList<>();
		Department dep = new Department();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectEmployee;
		JsonNode jsonObjectDepartment;
		JsonNode jsonObjectTeam;
		JsonNode jsonLoginAccount;

		Integer id = -1;

		try {
			jsonObjectEmployee = jsonMapper.readTree(json);
			jsonObjectTeam = jsonObjectEmployee.get("teams");
			jsonObjectDepartment = jsonObjectEmployee.get("departments");
			jsonLoginAccount = jsonObjectEmployee.get("user");
//			Check employee code existed
			String code = jsonObjectEmployee.get("code").asText();
			if(code.length() > 10) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE9") , ""));
			}
			String avatar = jsonObjectEmployee.get("avatar") != null ? jsonObjectEmployee.get("avatar").asText() : "";
			String gender = jsonObjectEmployee.get("gender") != null ? jsonObjectEmployee.get("gender").asText() : "";
			String dateOfBirth = jsonObjectEmployee.get("dateOfBirth") == null ? ""
					: jsonObjectEmployee.get("dateOfBirth").asText() == "null" ? ""
							: jsonObjectEmployee.get("dateOfBirth").asText();
			String email = jsonObjectEmployee.get("email") == null ? ""
					: jsonObjectEmployee.get("email").asText() == "null" ? ""
							: jsonObjectEmployee.get("email").asText();
			String phoneNumber = jsonObjectEmployee.get("phoneNumber") == null ? ""
					: jsonObjectEmployee.get("phoneNumber").asText() == "null" ? ""
							: jsonObjectEmployee.get("phoneNumber").asText();
			boolean isExisted = employeeRepository.checkEmployeeCodeExisted(id, code);

			if (isExisted) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE5"), ""));
			}
			emp.setCode(jsonObjectEmployee.get("code").asText());
			emp.setName(jsonObjectEmployee.get("name").asText());
			emp.setAvatar(avatar);
			emp.setGender(gender);
			emp.setDateOfBirth(dateOfBirth);
			emp.setEmail(email);
			emp.setPhoneNumber(phoneNumber);
			Boolean isEnableLogin = jsonLoginAccount.get("enableLogin").asBoolean();
			emp.setEnableLogin(isEnableLogin);
			if (isEnableLogin) {
				emp.setUsername(jsonLoginAccount.get("username").asText());
				emp.setPassword(encoder.encode(CMDConstrant.PASSWORD));
			} else {
				emp.setUsername("");
				emp.setPassword("");
			}
//			if (jsonObjectPosition.isArray()) {
//				for (JsonNode p : jsonObjectPosition) {
//					Position pos = new Position();
//					pos.setId(Integer.valueOf(p.toString()));
//					positionList.add(pos);
//				}
//			}
//			if (jsonObjectTeam.isArray()) {
//				for (JsonNode t : jsonObjectTeam) {
//					Team team = new Team();
//					team.setId(Integer.valueOf(t.toString()));
//					teamList.add(team);
//				}
//			}
//			
//			if (jsonObjectDepartment.isArray()) {
//				for (JsonNode d : jsonObjectDepartment) {
//					Department department = new Department();
//					department.setId(Integer.valueOf(d.toString()));
//					departmentList.add(department);
//				}
//			}
//			
//			for (JsonNode p : jsonObjectPosition) {
//				Position pos = new Position();
//				pos.setId(p.get("id").asInt());
//				positionList.add(pos);
//			}
			for (JsonNode t : jsonObjectTeam) {
				Integer teamId = t.get("id") !=null ? t.get("id").asInt() : -1;
				Team team = teamRepository.findById(teamId);
				if(team!=null) {
					Position pos = positionRepository.findById(t.get("position").get("id").asInt());
					positionList.add(pos);
					teamList.add(team);
				};
			}

			for (JsonNode d : jsonObjectDepartment) {
				Integer depId = d.get("id") != null ? d.get("id").asInt() : -1;
				Department department = departmentRepository.findById(depId);
				if(department!=null) {
					Position pos = positionRepository.findById(d.get("position").get("id").asInt());
					positionList.add(pos);
					departmentList.add(department);
					
				}
			}

			emp.setPositions(positionList);
			emp.setTeams(teamList);
			emp.setDepartments(departmentList);
			emp.setActiveFlag(true);
			emp.setActive(true);
			emp.setCreateDate(createDate);
			emp.setModifyDate(modifyDate);
			emp.setCreateBy(jsonObjectEmployee.get("createBy").asInt());
			emp.setModifyBy(jsonObjectEmployee.get("createBy").asInt());
			Integer idAdded = employeeRepository.add(emp);
			EmployeeModel employeeModel = toEmployeeModel(emp);
			if (idAdded != -1) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK",message.getMessageByItemCode("EMPS2") , employeeModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE4"), emp));

			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in addEmployee()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}

	}

	// API edit and clock account (isActive true || false)
	public ResponseEntity<Object> edit(String json) {
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Employee emp = new Employee();
		UserModel user = new UserModel();
		List<Position> positionList = new ArrayList<>();
		List<Team> teamList = new ArrayList<>();
		List<Department> departmentList = new ArrayList<>();
		Department dep = new Department();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObjectEmployee;
		JsonNode jsonObjectPosition;
		JsonNode jsonObjectTeam;
		JsonNode jsonObjectDepartment;
		JsonNode jsonLoginAccount;
		String modifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
		try {
			
			jsonObjectEmployee = jsonMapper.readTree(json);
			jsonObjectPosition = jsonObjectEmployee.get("positions");
			jsonObjectTeam = jsonObjectEmployee.get("teams");
			jsonObjectDepartment = jsonObjectEmployee.get("departments");
			Integer id = jsonObjectEmployee.get("id") != null ? jsonObjectEmployee.get("id").asInt() : -1;
			// Check with id if can edit or Update personal information
			if(!customRoleService.canUpdate("employee", userDetail) && !customRoleService.isTheSameUser(id, userDetail)) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR","Không có quyền chỉnh sửa", ""));
			}
			
			
			emp = employeeRepository.findById(id);
//			Check employee id existed
			String code = jsonObjectEmployee.get("code").asText();
			boolean isExisted = employeeRepository.checkEmployeeCodeExisted(id, code);
			if (isExisted) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE5"), ""));
			}
			jsonLoginAccount = jsonObjectEmployee.get("user");
			boolean active = jsonObjectEmployee.get("active").asBoolean();
			/*
			 * Check if active == false Check if the employee is the head of the department,
			 * do not allow the lock
			 */
			if (!active) {
				for (Position p : emp.getPositions()) {
					if (p.getIsManager()) {
						return ResponseEntity.status(HttpStatus.OK)
								.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE1"), ""));
					}
				}
			}
			String avatar = jsonObjectEmployee.get("avatar") != null ? jsonObjectEmployee.get("avatar").asText() : "";
			String gender = jsonObjectEmployee.get("gender") != null ? jsonObjectEmployee.get("gender").asText() : "";
			String dateOfBirth = jsonObjectEmployee.get("dateOfBirth") == null ? ""
					: jsonObjectEmployee.get("dateOfBirth").asText() == "null" ? ""
							: jsonObjectEmployee.get("dateOfBirth").asText();
			String email = jsonObjectEmployee.get("email") == null ? ""
					: jsonObjectEmployee.get("email").asText() == "null" ? ""
							: jsonObjectEmployee.get("email").asText();
			String phoneNumber = jsonObjectEmployee.get("phoneNumber") == null ? ""
					: jsonObjectEmployee.get("phoneNumber").asText() == "null" ? ""
							: jsonObjectEmployee.get("phoneNumber").asText();
			emp.setId(jsonObjectEmployee.get("id").asInt());
			emp.setCode(jsonObjectEmployee.get("code").asText());
			emp.setName(jsonObjectEmployee.get("name").asText());
			emp.setAvatar(avatar);
			emp.setGender(gender);
			emp.setDateOfBirth(dateOfBirth);
			emp.setEmail(email);
			emp.setPhoneNumber(phoneNumber);
			emp.setActive(jsonObjectEmployee.get("active").asBoolean());
			Boolean isEnableLogin = jsonLoginAccount.get("enableLogin").asBoolean();
			emp.setEnableLogin(isEnableLogin);
			// Cannot edit password
			if (isEnableLogin) {
				emp.setUsername(jsonLoginAccount.get("username").asText());
				emp.setPassword(encoder.encode(CMDConstrant.PASSWORD));
			} else {
				emp.setUsername("");
				emp.setPassword("");
			}
//			if (jsonObjectPosition.isArray()) {
//				for (JsonNode p : jsonObjectPosition) {
//					Position pos = new Position();
//					pos.setId(Integer.valueOf(p.toString()));
//					positionList.add(pos);
//				}
//			}
//			if (jsonObjectTeam.isArray()) {
//				for (JsonNode t : jsonObjectTeam) {
//					Team team = new Team();
//					team.setId(Integer.valueOf(t.toString()));
//					teamList.add(team);
//				}
//			}
//			if (jsonObjectDepartment.isArray()) {
//				for (JsonNode t : jsonObjectDepartment) {
//					Department department = new Department();
//					department.setId(Integer.valueOf(t.toString()));
//					departmentList.add(department);
//				}
//			}
//			for (JsonNode p : jsonObjectPosition) {
//				Position pos = new Position();
//				pos.setId(p.get("id").asInt());
//				positionList.add(pos);
//			}
			for (JsonNode t : jsonObjectTeam) {
				Integer teamId = t.get("id") !=null ? t.get("id").asInt() : -1;
				Team team = teamRepository.findById(teamId);
				if(team!=null) {
					Position pos = positionRepository.findById(t.get("position").get("id").asInt());
					positionList.add(pos);
					teamList.add(team);
				}
				
			}
			for (JsonNode d : jsonObjectDepartment) {
				Integer depId = d.get("id") != null ? d.get("id").asInt() : -1;
				Department department = departmentRepository.findById(depId);
				if(department!=null) {
					Position pos = positionRepository.findById(d.get("position").get("id").asInt());
					positionList.add(pos);
					departmentList.add(department);
					
				}
				
			}
			emp.setPositions(positionList);
			emp.setTeams(teamList);
			emp.setDepartments(departmentList);
			emp.setActiveFlag(true);
			emp.setActive(jsonObjectEmployee.get("active").asBoolean());
				emp.setCreateDate(jsonObjectEmployee.get("createDate").asText());
				emp.setModifyDate(modifyDate);
			emp.setCreateBy(jsonObjectEmployee.get("createBy").asInt());
			emp.setModifyBy(jsonObjectEmployee.get("modifyBy").asInt());
			Integer status =  employeeRepository.edit(emp);
			if (status != 0) {
				EmployeeModel employeeModel = toEmployeeModel(emp);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", message.getMessageByItemCode("EMPS3"), employeeModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE6"), emp));

			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in edit()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", e.getMessage(), ""));
		}

	}

	// Delete employee by id
	public ResponseEntity<Object> delete(Integer id) {
		try {
			Employee emp = employeeRepository.findById(id);
			for (Position p : emp.getPositions()) {
				if (p.getIsManager()) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE2"), ""));
				}
			}
			emp.setActive(false);
			emp.setActiveFlag(false);
			emp.getDepartments().clear();
			emp.getTeams().clear();
			emp.getPositions().clear();

			String updateStatus = employeeRepository.delete(emp);

			if (updateStatus.equals("1")) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", message.getMessageByItemCode("EMPS4"), id));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE7") + "", id));

			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", e.getMessage(),""));

		}

	}

	public ResponseEntity<Object> importEmployees(MultipartFile multipartFile) {
		try {
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			Path currentRelativePath = Paths.get("");
			String path = currentRelativePath.toAbsolutePath().toString();
			String pathFull = "";
			if(path.contains("jenkins")) {
				pathFull = path + "/CMD-BE/src/main/resources/Import";
			}else {
				pathFull = path + "/src/main/resources/Import";
			}
			if (countFile == 0) {
				File deleteAllFile = new File(pathFull);
				FileUtils.cleanDirectory(deleteAllFile); 
			}
			StringBuilder nameFile = new StringBuilder();
			nameFile.append("CMD-");
			nameFile.append(countFile);
			nameFile.append(".csv");
			File file = new File(pathFull + nameFile.toString());
			if(file.exists() && !file.isDirectory()) { 
				PrintWriter writer = new PrintWriter(file);
				writer.print("");
				writer.close();
			}
			multipartFile.transferTo(file);

			final File csvFile = new File(pathFull + nameFile.toString());
			CSVReader reader = new CSVReaderBuilder(new FileReader(pathFull + nameFile.toString()))
					.withSkipLines(1).build();

			Set<Employee> employees = reader.readAll().stream().map(data -> {
				Employee employee = new Employee();
				String name, email, dob, phone, dep, pos, gender, code;
				name = data[0];
				dob = data[1];
				email = data[2];
				phone = data[3];
				dep = data[4];
				pos = data[5];
				gender = data[6];
				code = data[7];
				List<Department> departments = new ArrayList<Department>();
				Department department = new Department();
				department = departmentRepository.findByName(dep);
				List<Position> positions = positionRepository.findAllByDepId(department.getId());
				List<Position> positionsEmp = new ArrayList<Position>();
				String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
				for (Position po : positions) {
					if (po.getName().equals(pos)) {
						positionsEmp.add(po);
					}
				}
				if (department != null) {
					departments.add(department);
				}
				employee.setName(name);
				employee.setDateOfBirth(dob);
				employee.setEmail(email);
				employee.setPhoneNumber(phone);
				employee.setDepartments(departments);
				employee.setPositions(positionsEmp);
				employee.setCreateDate(createDate);
				employee.setModifyDate(createDate);
				employee.setCode(code);
				employee.setCreateBy(userDetail.getId());
				employee.setModifyBy(userDetail.getId());
				employee.setActiveFlag(true);
				employee.setActive(true);
				employee.setPassword("cmdcmdcmd");
				employee.setEnableLogin(true);
				employee.setAvatar("https://i.imgur.com/bFbOCtQ.jpg");
				employee.setGender(gender);
				employee.setUsername(email);
				return employee;
			}).collect(Collectors.toSet());
			boolean success = employeeRepository.add(employees);
			
			if (success) {
				countFile++;
				String messageSuccess = message.getMessageByItemCode("EMPS1");
				
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", messageSuccess, ""));
			} else {
				String messageError = message.getMessageByItemCode("EMPE3");
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", messageError, ""));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in edit()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", "", ""));
		}

	}
	public static EmployeeModel toEmployeeModel(Employee employee) {
		EmployeeModel employeeModel = new EmployeeModel();
		List<PositionModel> positionModelList = new ArrayList<>();
		List<DepartmentModel> departmentModelList = new ArrayList<>();
		List<TeamModel> teamModelList= new ArrayList<>();
		if (employee!=null) {
			for (Position p : employee.getPositions()) {
				PositionModel positionModel = new PositionModel();
				
				Role role = new Role();
				role.setId(p.getRole().getId());
				role.setName(p.getRole().getName());
				positionModel.setId(p.getId());
				positionModel.setName(p.getName());
				positionModel.setIsManager(p.getIsManager());
				positionModel.setRole(role);
				if(p.getDepartment()!=null && p.getTeam()==null) {
					Department department = p.getDepartment();
					DepartmentModel departmentModel = new DepartmentModel();
					departmentModel.setId(department.getId());
					departmentModel.setCode(department.getCode());
					departmentModel.setName(department.getName());
					departmentModel.setFatherDepartmentId(department.getFatherDepartmentId());
					departmentModel.setHeadPosition(department.getHeadPosition());
					departmentModel.setDescription(department.getDescription());
					departmentModel.setLevel(department.getLevel());
					departmentModel.setPosition(positionModel);
					departmentModelList.add(departmentModel);
				}
				else if(p.getDepartment()==null && p.getTeam()!=null) {
					Team team = p.getTeam();
					TeamModel teamModel = new TeamModel();
					teamModel.setId(team.getId());
					teamModel.setCode(team.getCode());
					teamModel.setName(team.getName());
					teamModel.setDescription(team.getDescription());
					teamModel.setHeadPosition(team.getHeadPosition());
					teamModel.setPosition(positionModel);
					teamModelList.add(teamModel);
				}
			}
			UserModel user = new UserModel();
			user.setUsername(employee.getUsername());
			user.setEnableLogin(employee.isEnableLogin());
			employeeModel.setId(employee.getId());
			employeeModel.setCode(employee.getCode());
			employeeModel.setName(employee.getName());
			employeeModel.setAvatar(employee.getAvatar());
			employeeModel.setGender(employee.getGender());
			employeeModel.setDateOfBirth(employee.getDateOfBirth());
			employeeModel.setEmail(employee.getEmail());
			employeeModel.setPhoneNumber(employee.getPhoneNumber());
			employeeModel.setActive(employee.isActive());
			employeeModel.setCreateDate(employee.getCreateDate());
			employeeModel.setDepartments(departmentModelList);;
			employeeModel.setPositions(positionModelList);
			employeeModel.setUser(user);
			employeeModel.setCreateDate(employee.getCreateDate());
			employeeModel.setModifyDate(employee.getModifyDate());
			employeeModel.setCreateBy(employee.getCreateBy());
			employeeModel.setModifyBy(employee.getModifyBy());
			employeeModel.setTeams(teamModelList);
			return employeeModel;
		}
		return null;
	}
	public ResponseEntity<Object> findByName(String name){
		name = ((name != null) && name!="") ? name : "";
		Set<EmployeeModel> employeeModelSet = new LinkedHashSet<>();
		employeeModelSet = employeeRepository.findByName(name);
		Map<String, Object> result = new TreeMap<>();
		result.put("employees", employeeModelSet);
		if (employeeModelSet.size() > 0) {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", result));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Not found", result));
		}
	}
	public ResponseEntity<Object> findAllNotifies(String page, 
			String sort, String order, String keySearch) {
		List<NotifyModel> notifyModels = null;
		try {
			page = page == null ? "1" : page.trim();
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "id";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			Integer limit = CMDConstrant.LIMIT;
			int offset = (Integer.valueOf(page) - 1) * limit;
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			
			notifyModels = notifyRepositoryImpl.findByEmployeeId(userDetail.getId(), keySearch, offset, limit, sort, order);
			if (notifyModels.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", notifyModels));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Not found", notifyModels));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in edit()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", "", ""));
		}

	}
	public ResponseEntity<Object> allReadNotifies() {
		try {
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			Boolean result = notifyRepositoryImpl.allRead(userDetail.getId(),null);
			if (result) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", message.getMessageByItemCode("NOTIS1"), ""));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", message.getMessageByItemCode("NOTIE1"), ""));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in edit()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", "", ""));
		}
	}
	
	public ResponseEntity<Object> deleteNotifies(String json) {
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonNode = null;
		try {
			List<Integer> notifyIds = new ArrayList<Integer>();
			jsonNode = jsonMapper.readTree(json);
			if(null == jsonNode.get("notifyIds") ) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", message.getMessageByItemCode("NOTIE1"), ""));
			}
			JsonNode jsonStatusObject = jsonNode.get("notifyIds");

			for (JsonNode statusId : jsonStatusObject) {
				notifyIds.add(Integer.valueOf(statusId.toString()));
			}
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			Boolean result = notifyRepositoryImpl.delete(notifyIds);
			if (result) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", message.getMessageByItemCode("NOTIS1"), ""));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", message.getMessageByItemCode("NOTIE1"), ""));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in edit()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", "", ""));
		}
	}
	
	public ResponseEntity<Object> markIsReadNotifies(String json) {
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonNode = null;
		try {
			List<Integer> notifyIds = new ArrayList<Integer>();
			jsonNode = jsonMapper.readTree(json);
			if(null == jsonNode.get("notifyIds") ) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", message.getMessageByItemCode("NOTIE1"), ""));
			}
			JsonNode jsonStatusObject = jsonNode.get("notifyIds");

			for (JsonNode statusId : jsonStatusObject) {
				notifyIds.add(Integer.valueOf(statusId.toString()));
			}
			UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			Boolean result = notifyRepositoryImpl.allRead(userDetail.getId(),notifyIds);
			if (result) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", message.getMessageByItemCode("NOTIS1"), ""));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", message.getMessageByItemCode("NOTIE1"), ""));
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in edit()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseObject("ERROR", "", ""));
		}
	}
}
