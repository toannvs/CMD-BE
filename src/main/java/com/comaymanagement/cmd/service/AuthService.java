package com.comaymanagement.cmd.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.EmployeeModel;
import com.comaymanagement.cmd.model.LoginRequest;
import com.comaymanagement.cmd.model.OptionModel;
import com.comaymanagement.cmd.model.PermissionModel;
import com.comaymanagement.cmd.model.RoleDetailModel;
import com.comaymanagement.cmd.model.UserModel;
import com.comaymanagement.cmd.repository.UserRepository;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.RoleRepositoryImpl;
import com.comaymanagement.cmd.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	UserRepository userRepository;

	@Autowired
	Message message;

	@Autowired
	EmployeeRepositoryImpl employeeRepository;

	@Autowired
	RoleRepositoryImpl roleRepository;
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
		List<Integer> roleIds = new ArrayList<>();
		List<RoleDetailModel> roleDetailModels = new ArrayList<>();
		RoleDetailModel roleDetailModel = new RoleDetailModel();
		String jwt = "";
		Map<String, Object> result = new TreeMap<>();
		UserModel userModel = userRepository.findByUsername(loginRequest.getUsername());
		if (userModel == null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", message.getMessageByItemCode("LOGINE2"), ""));
		}
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		if (userDetails != null) {
			roleIds = roleRepository.findAllRoleIdByEmpId(userDetails.getId());
			for(Integer roleId : roleIds) {
				roleDetailModel = roleRepository.findRoleDetailByRoleId(roleId);
				roleDetailModels.add(roleDetailModel);
			}
			// Summary of role list
			RoleDetailModel roleDetailResult = new RoleDetailModel();
			
			roleDetailResult = roleDetailModels.get(0);
		  if(roleDetailModels.size()>1) {
				// check each role's permission
				for(int i =1; i<roleDetailModels.size();i++) {
					List<OptionModel> optionsModels  = roleDetailModels.get(i).getOptions();
					for(int j = 0;j < optionsModels.size(); j++) {
						List<PermissionModel> permissionModels = optionsModels.get(j).getPermissions();
						for(int k = 0; k < permissionModels.size(); k++) {
							PermissionModel permissionNeeded = roleDetailResult.getOptions().get(j).getPermissions().get(k);
							if(permissionModels.get(k).isSelected() && !permissionNeeded.isSelected()) {
								permissionNeeded.setSelected(true);
							}
						}
					}
				}
			}
			
			
			Employee employee = employeeRepository.findById(userDetails.getId());
			EmployeeModel employeeModel = EmployeeService.toEmployeeModel(employee);
			employeeModel.setRole(roleDetailResult);
//			userModel.setPassword(null);
//			userModel.setRoles(roleDetailModels);
			jwt = jwtUtils.generateJwtToken(userDetails);
			result.put("accessToken", jwt);
			result.put("userInfo", employeeModel);
		}
		
//		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
//				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ResponseObject("OK", message.getMessageByItemCode("LOGINS1"), result));
	}

	public ResponseEntity<Object> changePassword(String json) {
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		JsonMapper jsonMapper = new JsonMapper();
		Integer empId = null;
		String existingPassword = null;
		String newPassword = null;
		try {
			JsonNode jsonObject = jsonMapper.readTree(json);
			empId = jsonObject.get("id").asInt();
			existingPassword = jsonObject.get("existingPassword").asText();
			newPassword = jsonObject.get("newPassword").asText();
		} catch (Exception e) {
			LOGGER.error("Have error at changePassword();", e);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Cập nhật mật khẩu thất bại", ""));
		}
		// check if matches user id
		if (userDetail.getId().equals(empId)) {
			if (encoder.matches(existingPassword, userDetail.getPassword())) {
				String newPasswordEncoder = encoder.encode(newPassword);
				userDetail.setPassword(newPasswordEncoder);
				Employee employee = employeeRepository.findById(empId);
				employee.setPassword(newPasswordEncoder);
				employeeRepository.edit(employee);

			}else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Cập nhật mật khẩu thất bại", ""));
			}
		} else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "Cập nhật mật khẩu thất bại", ""));
		}
		return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "Cập nhật mật khẩu thành công",""));
	}

	public void resetPassword(String json) {

	}

}
