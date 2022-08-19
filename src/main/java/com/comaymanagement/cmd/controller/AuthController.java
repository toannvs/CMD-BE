package com.comaymanagement.cmd.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.model.LoginRequest;
import com.comaymanagement.cmd.service.AuthService;
/**
All option name
	todolist
	request
	type
	employee
	department
	position
	inventory
	team
All permission name
 	view
 	create
 	update
 	detele
 	view_all
 	update_all
 	delete_all
 **/

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Transactional(rollbackFor = Exception.class)
public class AuthController {
    @Autowired
    AuthService authService;
   
    
    @PostMapping("/signin")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

//    @PostMapping("/signup")
//    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest signUpRequest) {
//        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
//            return ResponseEntity
//                    .ok("B");
//            }
//
//        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//            return ResponseEntity
//                    .badRequest()
//                    .body("");
//        }
//
//        User user = new User();
//        user.setUsername(signUpRequest.getUsername());
//        user.setEmail(signUpRequest.getEmail());
//        user.setPassword(encoder.encode(signUpRequest.getPassword()));
//
//        Set<String> asignRoles = signUpRequest.getRole();
//        List<Role> roles = new ArrayList<>();
//
//        // Nếu không truyền thì set role mặc định là ROLE_USER
//        if (asignRoles == null) {
//        	Role userRole = new Role();
//        	userRole.setName("USER");
//        	roles.add(userRole);          
//        } else {
//            asignRoles.forEach(role -> {
//            	Role userRole = new Role();
//            	userRole.setName(role);
//            	roles.add(userRole);
//            });
//        }
//
//        user.setRoles(roles);
//        userRepository.save(user);
//
//        return ResponseEntity.ok("User registered successfully!");
//    }
    
//	@PreAuthorize(
//			"@customRoleService.canUpdate('employee', principal) "
//			+ "or @customRoleService.canUpdateAll('employee', principal)")
    @PostMapping("/change-password")
	@ResponseBody
	public ResponseEntity<Object> changePassword(@RequestBody String json){
     
     return authService.changePassword(json);
    }
    
	@PostMapping("/forgot_password")
	public ResponseEntity<Object> processForgotPassword(@RequestBody String json) {
		return authService.updateResetPasswordToken(json);
	}
	
	@GetMapping("/reset_password")
	public ResponseEntity<Object> checkToken(@RequestParam String token) {
		return authService.checkToken(token);
	}
	
	@PostMapping("/reset_password")
	public ResponseEntity<Object> resetPassword(@RequestBody String json) {
		return authService.updatePassword(json);
	}
	
}