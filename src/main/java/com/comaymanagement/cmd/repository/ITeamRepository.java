package com.comaymanagement.cmd.repository;

import java.util.Set;

import com.comaymanagement.cmd.entity.Team;
import com.comaymanagement.cmd.model.TeamModel;

public interface ITeamRepository {
	public Set<TeamModel> findAll(String name);
	public Integer add(Team dep);
	public Integer edit(Team dep);
	public String delete(Integer id);
	public boolean isExisted(Integer id, String code);
	public Team findById(Integer id);
	public Team findByName(String name);
}
