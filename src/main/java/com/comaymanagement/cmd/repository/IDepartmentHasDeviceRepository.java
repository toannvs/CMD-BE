package com.comaymanagement.cmd.repository;

import java.util.Set;

import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.DepartmentHasDevice;
import com.comaymanagement.cmd.entity.Device;
import com.comaymanagement.cmd.model.DeviceModel;

public interface IDepartmentHasDeviceRepository {
	public Set<Department> findAllDeviceByDepartmentName(String name);
	public DeviceModel add(DepartmentHasDevice departmentHasDevice);
	public Device findById(Integer id);
	public DeviceModel edit(DepartmentHasDevice departmentHasDevice);
	public DepartmentHasDevice findDepartmentHasDeviceById(Integer id);
	public Boolean delete(DepartmentHasDevice departmentHasDevice);
}
