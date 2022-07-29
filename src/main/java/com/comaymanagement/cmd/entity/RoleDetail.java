package com.comaymanagement.cmd.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
@Entity(name = "role_details")
@JsonInclude(Include.NON_NULL)
public class RoleDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(name="create_by")
	private Integer createBy;
	@Column(name="modify_by")
	private Integer modifyBy;
	@Column(name="create_date")
	private String createDate;
	@Column(name="modify_date")
	private String modifyDate;
//
//	@OneToOne()
//	@JoinColumn(name = "option_id")
//	private Option option;
//
//	@OneToOne()
//	@JoinColumn(name = "permission_id")
//	private Permission permission;
//
//	@OneToOne()
//	@JoinColumn(name = "role_id")
//	private Role role;
	@Column(name = "option_id")
	private Integer optionId;
	@Column(name = "permission_id")
	private Integer permissionId;

//	private Role role;
//	@OneToOne
	@Column(name = "role_id")
	private Integer roleId;
}
