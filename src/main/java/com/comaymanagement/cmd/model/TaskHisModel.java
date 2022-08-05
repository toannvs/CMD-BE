package com.comaymanagement.cmd.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskHisModel {
	private String message;
	private String modifyDate;
	private String modifyBy;
	private Integer modifyById;
	private String avatar;
}
