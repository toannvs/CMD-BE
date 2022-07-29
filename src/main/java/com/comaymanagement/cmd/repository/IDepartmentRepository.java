package com.comaymanagement.cmd.repository;

import java.util.List;
import java.util.Set;

import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.model.DepartmentModel;

public interface IDepartmentRepository{
	public Set<DepartmentModel> findAll(String name);
	public Integer add(Department dep);
	public Integer edit(Department dep);
	public String delete(Integer id);
	public boolean isExisted(Integer id, String code);
	public Department findById(Integer id);
	public Department findByName(String name);
	public List<Department> findAllByEmployeeId(Integer empId);
}
