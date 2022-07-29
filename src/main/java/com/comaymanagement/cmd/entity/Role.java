package com.comaymanagement.cmd.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "roles")
@JsonInclude(Include.NON_NULL)
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	
//	@OneToMany
//	@JoinColumn(name = "role_id")
//	@JsonBackReference
//	private Set<Auth> auths;
//
//	@OneToMany()
//	@JoinColumn(name = "role_id")
//	@JsonBackReference
//	private List<RoleDetail> roleDetails;

	@OneToMany()
	@JoinColumn(name = "role_id")
	@JsonBackReference
	private List<Position> positions;
}
