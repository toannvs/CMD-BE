package com.comaymanagement.cmd.entity;

import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Entity(name = "teams")
@JsonInclude(Include.NON_NULL)
public class Team {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String code;
	private String name;
	@Column(name="head_position")
	private Integer headPosition;
	@Column(name="create_by")
	private Integer createBy;
	@Column(name="create_date")
	private String createDate;
	@Column(name="modify_by")
	private Integer modifyBy;
	@Column(name="modify_date")
	private String modifyDate;
	@Column(name="description")
	private String description;
	@ManyToMany
	@JoinTable(name = "teams_employees", joinColumns = {
			@JoinColumn(name = "team_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "employee_id", referencedColumnName = "id") })
	@JsonBackReference
	private Set<Employee> employees;

	@OneToMany
	@JoinColumn(name = "team_id")
	@JsonBackReference
	private List<Position> positions;
}
