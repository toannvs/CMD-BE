package com.comaymanagement.cmd.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "notify")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notify {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;
	String title;
	String description;
	
	@OneToOne
	@JoinColumn(name = "employee_id")
	Employee receiver;
	
	@Column(name = "is_read")
	Boolean isRead;
	
	String type;
	
	@Column(name = "type_detail_id")
	Integer detailId;
	
	
}
