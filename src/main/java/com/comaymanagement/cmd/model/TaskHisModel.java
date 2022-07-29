package com.comaymanagement.cmd.model;

import com.comaymanagement.cmd.entity.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskHisModel {

	private Integer id;
	private TaskModel task;
	private EmployeeModel receiver;
	private Status status;
	private String modifyDate;
}
