package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.TaskHis;

public interface ITaskHistory {
	Integer add(TaskHis taskHis);
	List<TaskHis> findById(Integer id);
}
