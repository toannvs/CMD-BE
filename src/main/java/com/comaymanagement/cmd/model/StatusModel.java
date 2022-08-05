package com.comaymanagement.cmd.model;

import java.util.Set;

import javax.persistence.Entity;

import com.comaymanagement.cmd.entity.Proposal;
import com.comaymanagement.cmd.entity.Task;
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
public class StatusModel {
	private Integer id;
	private String name;
	private Integer index;
	private String type;
	private Integer countByStatus;
}
