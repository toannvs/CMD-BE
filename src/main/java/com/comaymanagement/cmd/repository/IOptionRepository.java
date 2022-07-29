package com.comaymanagement.cmd.repository;

import java.util.List;
import java.util.Set;

import com.comaymanagement.cmd.entity.Option;
import com.comaymanagement.cmd.model.OptionModel;

public interface IOptionRepository {
	public List<OptionModel> findAll();
	public Set<Option> findByRoleId(Integer roleId);
}
