package com.comaymanagement.cmd.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.model.PositionModel;

public interface IPositionRepository{
	List<PositionModel> findAllByDepartmentId(Integer depId);
	List<PositionModel> findAllByRoleId(
			@Param("roleID") Integer roleId);
	Integer CountTotalItem();
	Integer add(Position p);
	Integer edit(Position p);
	Position findById(Integer id);
	List<Position> findAllByDepId(Integer depId);
	List<PositionModel> findAllByTeamId(Integer teamId);
	List<Position> findAllByEmployeeId(Integer empId);
}
