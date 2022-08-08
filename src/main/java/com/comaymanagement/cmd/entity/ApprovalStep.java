package com.comaymanagement.cmd.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "approval_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ApprovalStep {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(name="approval_step_index")
	private Integer approvalStepIndex;
	@Column(name="approval_step_name")
	private String approvalStepName;

	@OneToOne()
	@JoinColumn(name = "proposal_type_id")
	private ProposalType proposalType;

	@OneToMany
	@JoinColumn(name = "approval_step_id")
	@JsonBackReference
	private Set<ApprovalStepDetail> approvalStepDetails;
}
