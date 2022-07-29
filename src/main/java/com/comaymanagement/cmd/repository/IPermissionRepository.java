package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.model.PermissionModel;

public interface IPermissionRepository {
	List<PermissionModel> findAll();
}
