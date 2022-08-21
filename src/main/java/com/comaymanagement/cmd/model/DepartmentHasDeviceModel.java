package com.comaymanagement.cmd.model;

import com.comaymanagement.cmd.entity.Device;
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
@JsonInclude(Include.NON_NULL)
public class DepartmentHasDeviceModel {
	private Integer id;
	Device device;
	private String description;
	private Boolean isActive;
}
