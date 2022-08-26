package com.comaymanagement.cmd.model;

import javax.persistence.Entity;

import com.comaymanagement.cmd.entity.Employee;
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
public class PostModel {
	private int id;
	private String title;
	private String content;
	private boolean isPulished;
	private Employee creator;
	private Employee editor;
	private String createDate;
	private String modifyDate;
	private Integer likeTotal;
	private boolean isLike;
}
