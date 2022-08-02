package com.comaymanagement.cmd.entity;

import java.util.Set;

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
@Entity(name = "statuses")
@JsonInclude(Include.NON_NULL)
public class Status {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private Integer index;
	private String type;
	
	@OneToMany
	@JoinColumn(name = "status_id")
	@JsonBackReference
	private Set<Proposal> proposalList;

	@OneToMany
	@JoinColumn(name = "status_id")
	@JsonBackReference
	private Set<Task> taskList;
}
