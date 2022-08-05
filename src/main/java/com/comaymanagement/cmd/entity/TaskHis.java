package com.comaymanagement.cmd.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "task_his")
@JsonInclude(Include.NON_NULL)
public class TaskHis {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "task_id")
	private Integer taskId;
	
	@OneToOne
	@JoinColumn(name = "receiver_id")
	private Employee receiver;
	
	@OneToOne
	@JoinColumn(name = "status_id")
	private Status status;
	
	@Column(name= "modify_date")
	private String modifyDate;
	
	@OneToOne
	@JoinColumn(name= "modify_by")
	private Employee modifyBy;
	
	@Column(name= "message")
	private String message;
	
}
