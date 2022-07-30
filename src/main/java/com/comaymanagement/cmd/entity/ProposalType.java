package com.comaymanagement.cmd.entity;

import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

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
@Entity(name = "proposal_types")
@JsonInclude(Include.NON_NULL)         
public class ProposalType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	@Column(name="active_flag")
	private boolean activeFlag;
	
	@OneToMany
	@JoinColumn(name = "proposal_type_id")
	@JsonBackReference
	private List<ProposalPermission> proposalPermissions;

	@OneToMany
	@JoinColumn(name = "proposal_type_id")
	@JsonBackReference
	private List<Proposal> proposals;

	@OneToMany
	@JoinColumn(name = "proposal_type_id")
	@JsonBackReference
	private List<ApprovalStep> approvalSteps;

}
