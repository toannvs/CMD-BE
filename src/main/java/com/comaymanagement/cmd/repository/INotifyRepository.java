package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.Notify;
import com.comaymanagement.cmd.model.NotifyModel;

public interface INotifyRepository {
	List<NotifyModel> findByEmployeeId(Integer employeeId, String keySearch, Integer offset, Integer limit, String sort, String order);
	Integer add(Notify notify);
	boolean allRead(Integer employeeId, List<Integer> notifyIds);
	boolean delete(List<Integer> notifyIds);
	Notify findById(Integer notifyId);
	List<Notify> findByEmployeeIdToEdit(Integer employeeId);
}
