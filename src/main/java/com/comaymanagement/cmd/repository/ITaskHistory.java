package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.TaskHis;

public interface ITaskHistory {
	List<TaskHis> add(List<TaskHis> taskHis);
	List<TaskHis> findById(Integer id);
}
