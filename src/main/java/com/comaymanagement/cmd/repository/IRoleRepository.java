package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.model.RoleDetailModel;
import com.comaymanagement.cmd.model.RoleModel;

public interface IRoleRepository {
	List<RoleModel> findAll(
			String name,
			String sort,
			String order,
			Integer limit,
			Integer offset);
	Integer countAllPaging(String name, String sort, String order, Integer limit, Integer offset);
	public RoleDetailModel findRoleDetailByRoleId(Integer roleId);
	public Integer add(Role role);
	public Integer edit(Role role);
	public Role findById(Integer id);
	public Integer delete(Integer id);
	public List<Integer> findAllRoleIdByEmpId(Integer empId);
}
