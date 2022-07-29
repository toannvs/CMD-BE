package com.comaymanagement.cmd.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.comaymanagement.cmd.entity.DataType;
import com.comaymanagement.cmd.entity.ProposalType;
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
public class ProposalTypeDetailModel {
//	private ProposalType proposalType;
	// Array of field
	private Integer id; // field id
	private String name;
	private String label;
	private String placeholder;
	private String description;
	private boolean isRequired;
	private String feedback;
	private DataType dataType;
	
//	List<ApprovalStepModel> approvalStepModels; 
}

