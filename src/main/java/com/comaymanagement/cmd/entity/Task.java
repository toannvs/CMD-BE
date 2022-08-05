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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "tasks")
@JsonInclude(Include.NON_NULL)
public class Task{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String title;
	private String description;
	@Column(name= "create_date")
	private String createDate;
	@Column(name= "finish_date")
	private String finishDate;
	@Column(name= "start_date")
	private String startDate;
	@Column(name= "modify_date")
	private String modifyDate;
	@OneToOne()
	@JoinColumn(name = "creator_id")
	private Employee creator;

	@OneToOne()
	@JoinColumn(name = "receiver_id")
	private Employee receiver;

	@OneToOne()
	@JoinColumn(name = "status_id")
	private Status status;
	
	private Integer rate;
	private Integer priority;
	

}
