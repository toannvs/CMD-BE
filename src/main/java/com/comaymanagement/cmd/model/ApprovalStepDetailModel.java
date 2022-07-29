package com.comaymanagement.cmd.model;

import javax.persistence.Entity;

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
@Entity
@JsonInclude(Include.NON_NULL)
public class ApprovalStepDetailModel {
	private String stepName;
	private String stepIndex;
	private Integer typeId;	
}
