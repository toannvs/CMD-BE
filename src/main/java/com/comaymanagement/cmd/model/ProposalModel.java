package com.comaymanagement.cmd.model;

import java.util.List;

import javax.persistence.Column;

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
	private ProposalType proposal;
	private List<ContentModel> contents;
	private String createdDate;
	private Status status;
	private Integer currentStep;
}
