package com.comaymanagement.cmd.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.comaymanagement.cmd.constant.CMDConstrant;
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

import net.bytebuddy.utility.RandomString;

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
    private JavaMailSender mailSender;
	
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
			Map<String, Object> optionMap = convertRoleForFEGantPermission(roleDetailModel);
			employeeModel.setRole(optionMap);
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
	public Map<String, Object> convertRoleForFEGantPermission(RoleDetailModel roleDetailModel){
	
		Map<String, Object> optionMaps = new LinkedHashMap<>();
		for(OptionModel opModel : roleDetailModel.getOptions()) {
			Map<String, Object> permissionMaps = new LinkedHashMap<>();
			for(PermissionModel perModel : opModel.getPermissions()) {
				permissionMaps.put(perModel.getName(), perModel.isSelected());
			}
			optionMaps.put(opModel.getName(), permissionMaps);
		}
		return optionMaps;
	}
	
	public ResponseEntity<Object> updateResetPasswordToken(String json){
    	String email  = null;
    	JsonNode jsonNode = null;
    	JsonMapper jsonMapper = new JsonMapper();
    	try {
    		jsonNode = jsonMapper.readTree(json);
    		email = (jsonNode.get("email") == null || jsonNode.get("email").asText().equals("")) ? "" : jsonNode.get("email").asText();
    		if (email.equals("")) {
				
			}
			Employee employee = employeeRepository.findByEmail(email);
			String token = RandomString.make(30);
			if(null != employee) {
				employee.setResetPasswordToken(token);
				employeeRepository.edit(employee);
			}
			String resetPasswordLink = CMDConstrant.LOCAL_LINK + "/api/auth/reset_password?token=" + token;
			sendEmail(employee.getEmail(),resetPasswordLink);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "Đã gửi link reset password đến email", ""));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", "Đã có lỗi xảy ra", ""));
		}

    }
     
	public void sendEmail(String recipientEmail, String link)
	        throws MessagingException, UnsupportedEncodingException {
	    MimeMessage message = mailSender.createMimeMessage();              
	    MimeMessageHelper helper = new MimeMessageHelper(message);
	     
	    helper.setFrom("nguyenminhdungtd98@gmail.com", "Admin Support");
	    helper.setTo(recipientEmail);
	     
	    String subject = "Here's the link to reset your password";
	     
	    String content = "<p>Hello,</p>"
	            + "<p>You have requested to reset your password.</p>"
	            + "<p>Click the link below to change your password:</p>"
	            + "<p><a href=\"" + link + "\">Change my password</a></p>"
	            + "<br>"
	            + "<p>Ignore this email if you do remember your password, "
	            + "or you have not made the request.</p>";
	     
	    helper.setSubject(subject);
	     
	    helper.setText(content, true);
	     
	    mailSender.send(message);
	}
	public ResponseEntity<Object> checkToken(String token) {
    	Employee employee = null;
    	try {
			employee = employeeRepository.findByResetPasswordToken(token);
			if(employee == null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "Token invalid", ""));
			}
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "Successful authentication", employee));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", "Đã có lỗi xảy ra", e.getMessage()));
		}
    }
     
	public ResponseEntity<Object> updatePassword(String json) {
    	JsonNode jsonNode = null;
    	JsonMapper jsonMapper = new JsonMapper();
    	Employee employee = null;
    	Integer id = null;
    	String newPassword = null;
		try {
			jsonNode = jsonMapper.readTree(json);
			id = jsonNode.get("id").asInt();
			newPassword = (jsonNode.get("newPassword") == null || jsonNode.get("newPassword").asText().equals("")) ? "" : jsonNode.get("newPassword").asText();
			
			employee = employeeRepository.findById(id);
			if(null == employee) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("EMPE8"), ""));
			}
	    	employee.setPassword(encoder.encode(newPassword));
	    	employee.setResetPasswordToken(null);
	    	
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "Reset password successfully", ""));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", "Đã có lỗi xảy ra", ""));
		}

    }
    
    
}
