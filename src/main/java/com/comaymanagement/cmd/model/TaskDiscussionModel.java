package com.comaymanagement.cmd.model;

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
@JsonInclude(Include.NON_NULL)
public class TaskDiscussionModel {
	private Integer id;
	private String content;
	private String modifyDate;
	private String modifyBy;
	private Integer modifyById;
	private String avatar;
}
