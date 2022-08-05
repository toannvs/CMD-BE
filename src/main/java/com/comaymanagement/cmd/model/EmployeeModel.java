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
public class EmployeeModel {
	private Integer id;
	private String code;
	private String name;
	private String dateOfBirth;
	private String email;
	private String phoneNumber;
	private String avatar;
	private String gender;
	private List<PositionModel> positions;
	private List<DepartmentModel> departments;
	private List<TeamModel> teams;
	private UserModel user;
	private boolean active;
	private String createDate;
	private String modifyDate;
	private Integer createBy;
	private Integer modifyBy;
	private List<RoleDetailModel> roles;
	private RoleDetailModel role;
}
