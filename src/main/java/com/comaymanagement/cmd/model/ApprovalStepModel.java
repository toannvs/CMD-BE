package com.comaymanagement.cmd.model;

import java.util.List;

import javax.persistence.Entity;

import com.comaymanagement.cmd.entity.ApprovalOption_View;
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
public class ApprovalStepModel {
	private Integer id;
	private Integer index;
	private String name;
	private List<ApprovalOption_View>  approvalConfigTargets;
}
