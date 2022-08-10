package com.comaymanagement.cmd.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.comaymanagement.cmd.entity.Task;
import com.comaymanagement.cmd.entity.TaskHis;
import com.comaymanagement.cmd.model.TaskModel;


public interface ITaskRepository {
	
	List<TaskModel> findByStatusId(
			@Param("status_id") String statusId,
			@Param("sort") String sort,
			@Param("order") String order,
			@Param("offset") Integer offset,
			@Param("limit") Integer limit);
	
	List<TaskModel> findAll(List<Integer> departmentIds, String title, List<Integer> status, List<Integer> creators, List<Integer> receivers,
			String startDate, String finishDate,String priority, String rate, String sort, String order,Integer offset, Integer limit);
	Integer countAllPaging(List<Integer> departmentIds, String title, List<Integer> status, List<Integer> creators, List<Integer> receivers,
			String startDate, String finishDate,String priority, String rate, String sort, String order,Integer offset, Integer limit);
	Integer countFindByIds(List<Integer> ids);
	List<TaskModel> findByStatusIds(
			@Param("status_id") List<Integer> statusIds,
			@Param("sort") String sort,
			@Param("order") String order,
			@Param("offset") Integer offset,
			@Param("limit") Integer limit);
	TaskModel add(Task task) throws Exception;
	Integer getMaxId();
	TaskModel findById(Integer id);
	Task findByIdToEdit(Integer id);
	String deleteTaskById(Integer id);
	TaskModel edit(Task task,boolean updateTask, boolean changeStatus, boolean reopen,String reason) throws Exception;
	List<TaskModel> filter(
			@Param("createFrom") String createFrom,
			@Param("createTo") String createTo,
			@Param("finishFrom") String finishFrom,
			@Param("finishTo") String finishTo,
			@Param("title") String title,
			@Param("creator") String creator,
			@Param("receicer") String receiver,
			@Param("department") String department,
			@Param("limit") Integer limit,
			@Param("order") String order,
			@Param("page") String page,
			@Param("sort") String sort
	);
	Integer countFilter(
			@Param("createFrom") String createFrom,
			@Param("createTo") String createTo,
			@Param("finishFrom") String finishFrom,
			@Param("finishTo") String finishTo,
			@Param("title") String title,
			@Param("creator") String creator,
			@Param("receicer") String receiver,
			@Param("department") String department
	);
	List<TaskHis> findAllHistoryByTaskID(Integer taskId);
	
	List<TaskModel> findAllTaskAssigeToMe(Integer employeeId,List<Integer> creatorIds,
			List<Integer> departmentIds, List<Integer> statusIds,Integer rate,String startDate,
			String finishDate, String sort, String order, Integer offset, Integer limit);
	
	Integer countAllTaskAssigeToMe(Integer employeeId,List<Integer> creatorIds,
			List<Integer> departmentIds, List<Integer> statusIds,Integer rate,String startDate,
			String finishDate, String sort, String order, Integer offset, Integer limit);
	
	List<TaskModel> findAllTaskCreatedByMe(Integer employeeId,List<Integer> receiverIds,
			List<Integer> departmentIds, List<Integer> statusIds,Integer rate,String startDate,
			String finishDate, String sort, String order, Integer offset, Integer limit);
	
	Integer countAllTaskCreatedByMe(Integer employeeId,List<Integer> receiverIds,
			List<Integer> departmentIds, List<Integer> statusIds,Integer rate,String startDate,
			String finishDate, String sort, String order, Integer offset, Integer limit);
	void ScanOverDueTask();
}
