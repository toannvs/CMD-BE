package com.comaymanagement.cmd.model;

import com.comaymanagement.cmd.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PositionModel {
	private Integer id;
	private String code;
	private String name;
	private Boolean isManager;
	private Role role;
	private DepartmentModel department;
	private TeamModel team;
	
}
