package com.comaymanagement.cmd.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.entity.Status;

@Repository
public interface IStatusRepositoty {
	Status findById(Integer id);
	List<Status> findAll();
}
