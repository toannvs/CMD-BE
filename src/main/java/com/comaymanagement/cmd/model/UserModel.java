package com.comaymanagement.cmd.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserModel {
	private Integer id;
	private String username;
	private String password;
	private boolean enableLogin;
	private String email;
	private List<RoleDetailModel> roles;
	
}
