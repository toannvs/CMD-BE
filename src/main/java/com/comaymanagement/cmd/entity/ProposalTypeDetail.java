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
@Entity(name = "proposal_type_details")
@JsonInclude(Include.NON_NULL)
public class ProposalTypeDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(name="field_id")
	private String fieldId;
	@Column(name="field_name")
	private String name;
	@Column(name="label")
	private String label;
	@Column(name="create_by")
	private Integer createBy;
	@Column(name="modify_by")
	private Integer modifyBy;
	@Column(name="create_date")
	private String createDate;
	@Column(name="modify_date")
	private String modifyDate;

	@OneToOne()
	@JoinColumn(name = "proposal_type_id")
	private ProposalType proposalType;

	@OneToOne()
	@JoinColumn(name = "data_type_id")
	private DataType dataType;
	private String placeholder;
	private String description;
	@Column(name="is_required")
	private boolean isRequired;
	private String feedback;
	
}
