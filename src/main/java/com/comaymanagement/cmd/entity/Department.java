package com.comaymanagement.cmd.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Department {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String code;
	private String name;
	@Column(name="farther_department_id")
	private Integer fatherDepartmentId;
	@Column(name="head_position")
	private Integer headPosition;
	@Column(name="create_by")
	private Integer createBy;
	@Column(name="create_date")
	private String createDate;
	@Column(name="modify_by")
	private Integer modifyBy;
	@Column(name="modify_date")
	private String modifyDate;
	@Column(name="description")
	private String description;
	private Integer level;
	
	@OneToMany()
	@JoinColumn(name = "department_id")
	private List<Position> positions;

	@OneToMany
	@JoinColumn(name = "department_id")
	private List<ProposalPermission> proposalPermissions;

	@OneToMany
	@JoinColumn(name = "department_id")
	private List<ApprovalStepDetail> approvalStepDetails;
	
	@ManyToMany()
	@JoinTable(name = "departments_employees", joinColumns = {
			@JoinColumn(name = "department_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "employee_id", referencedColumnName = "id") })
	private List<Employee> employees;

}
