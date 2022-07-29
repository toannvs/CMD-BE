package com.comaymanagement.cmd.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String title;
	private String content;
	@Column(name="is_pulished")
	private boolean isPulished;

	@OneToOne
	@JoinColumn(name="create_by")
	private Employee creator;
	@OneToOne
	@JoinColumn(name="modify_by")
	private Employee editor;
	@Column(name="create_date")
	private String createDate;
	@Column(name="modify_date")
	private String modifyDate;
	@Column(name="like_total")
	private Integer likeTotal;
	
	@ManyToMany()
	@JoinTable(name = "post_favourite", joinColumns = {
			@JoinColumn(name = "post_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "employee_id", referencedColumnName = "id") })
	private List<Employee> employees;
}
