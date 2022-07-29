package com.comaymanagement.cmd.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.DataType;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.repositoryimpl.DataTypeRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class DataTypeService {
	@Autowired
	DataTypeRepository dataTypeRepository;
	
	public ResponseEntity<Object> findAll() {
		List<DataType> dataTypes = dataTypeRepository.findAll();
		if (dataTypes.size() > 0) {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", dataTypes));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Not found", ""));
		}

	}
	
}
