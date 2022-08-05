package com.comaymanagement.cmd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.OptionModel;
import com.comaymanagement.cmd.repositoryimpl.OptionRepositoryImpl;

@Service
@Transactional(rollbackFor = Exception.class)
public class OptionService {
	@Autowired
	OptionRepositoryImpl optionRepository;
	
	public ResponseEntity<Object> findAllWithPermissionDefault() {
		List<OptionModel> optionModels = new ArrayList<OptionModel>();;
		optionModels = optionRepository.findAllWithPermissionDefault();
		if(optionModels!=null && optionModels.size()>0) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "OK", optionModels));
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ResponseObject("ERROR", "Not found", ""));
	}
}
