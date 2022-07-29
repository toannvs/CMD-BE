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
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Entity(name = "employees")
@JsonInclude(Include.NON_NULL)
public class Employee{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String code;
	private String name;
	@Column(name="date_of_birth")
	private String dateOfBirth;
	private String email;
	@Column(name="phone_number")
	private String phoneNumber;
	@Column(name="active_flag")
	private boolean activeFlag;
	@Column(name="create_by")
	private Integer createBy;
	@Column(name="modify_by")
	private Integer modifyBy;
	@Column(name="create_date")
	private String createDate;
	@Column(name="modify_date")
	private String modifyDate;
	private String avatar;
	private String gender;
	private String username;
	private String password;
	@Column(name="enable_login")
	private boolean enableLogin;
	@Column(name="is_active")
	private boolean active;
	
	@ManyToMany()
	@JsonIgnore
	@JoinTable(name = "departments_employees", joinColumns = {
			@JoinColumn(name = "employee_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "department_id", referencedColumnName = "id") })
	private List<Department> departments;
	
	@ManyToMany()
	@JsonIgnore
	@JoinTable(name = "positions_employees", joinColumns = {
			@JoinColumn(name = "employee_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "position_id", referencedColumnName = "id") })
	private List<Position> positions;

	@OneToMany()
	@JsonIgnore
	@JoinColumn(name = "employee_id")
	private List<ProposalPermission> proposalPermissions;

	@OneToMany()
	@JsonIgnore
	@JoinColumn(name = "creator_id")
	private List<Task> taskListCreated;
	
	@OneToMany()
	@JsonIgnore
	@JoinColumn(name = "receiver_id")
	private List<Task> taskListReceived;

	@OneToMany()
	@JsonIgnore
	@JoinColumn(name = "creator_id")
	private List<Proposal> proposals;

	@OneToMany()
	@JsonIgnore
	@JoinColumn(name = "employee_id")
	private List<ApprovalStepDetail> approvalStepDetails;
	
	@ManyToMany
	@JsonIgnore
	@JoinTable(name = "teams_employees", joinColumns = {
			@JoinColumn(name = "employee_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "team_id", referencedColumnName = "id") })
	private List<Team> teams;

	@ManyToMany()
	@JsonIgnore
	@JoinTable(name = "post_favourite", joinColumns = {
			@JoinColumn(name = "employee_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "post_id", referencedColumnName = "id") })
	private List<Post> posts;
}
