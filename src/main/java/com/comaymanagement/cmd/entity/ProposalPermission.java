package com.comaymanagement.cmd.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "proposals_permissions")
@JsonInclude(Include.NON_NULL)
public class ProposalPermission {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "position_id")
	private Integer positionId;

	@Column(name = "department_id")
	private Integer departmentId;

	@Column(name = "employee_id")
	private Integer employeeId;
	
	@OneToOne()
	@JoinColumn(name = "proposal_type_id")
	private ProposalType proposalType;
}
