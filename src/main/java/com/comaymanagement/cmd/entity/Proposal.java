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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "proposals")
@JsonInclude(Include.NON_NULL)
public class Proposal {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@OneToMany
	@JoinColumn(name = "proposal_id")
	@JsonBackReference
	private Set<ProposalDetail> proposalDetails;

	@OneToOne()
	@JoinColumn(name = "creator_id")
	private Employee creator;

	@OneToOne()
	@JoinColumn(name = "status_id")
	private Status status;

	@OneToOne()
	@JoinColumn(name = "proposal_type_id")
	private ProposalType proposalType;
	
	@Column(name="modify_by")
	private Integer modifyBy;
	@Column(name="create_date")
	private String createDate;
	@Column(name="modify_date")
	private String modifyDate;
	@Column(name="current_step")
	private Integer currentStep;
	
}
