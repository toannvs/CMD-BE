package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.RoleDetail;

public interface IRoleDetailRepository {
	public Integer add(RoleDetail roleDetail);
	public Integer delete(Integer id);
	public List<RoleDetail> findAllByRoleId(Integer roleId);
}
