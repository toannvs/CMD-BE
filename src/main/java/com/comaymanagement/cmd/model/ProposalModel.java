package com.comaymanagement.cmd.model;

import java.util.List;

import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.Status;
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
public class ProposalModel {
	private int id;
	private EmployeeModel creator;
	private ProposalType proposalType;
	private List<ContentModel> contents;
	private String createdDate;
	private Status status;
	private Integer currentStep;
	private String reason;
	private boolean canApprove;
}
