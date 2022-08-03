package com.comaymanagement.cmd.model;

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
public class ProposalTypeModel {
	private Integer id;
	private String name;
	private boolean activeFlag;
	private String createDate;
}
