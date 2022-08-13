package com.comaymanagement.cmd.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.ApprovalOption_View;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.repositoryimpl.ApprovalOption_ViewRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApprovalOption_ViewService {
	@Autowired
	Message message;
	@Autowired
	ApprovalOption_ViewRepository approvalOptionReposiroty;
	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalOption_ViewService.class);
	public ResponseEntity<Object> findAll(String name) {
		name = name != null ? name.trim() : "";
		List<ApprovalOption_View> approvalOption_Views = approvalOptionReposiroty.findAll(name);
		
		if (approvalOption_Views != null) {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", approvalOption_Views));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Not found", approvalOption_Views));
		}

	}
	public ResponseEntity<Object> findById(Integer id, String table) {
		id = id != null ? id : -1;
		table = table !=null ? table : "";
		ApprovalOption_View approvalOption_View = approvalOptionReposiroty.findById(id, table);
		
		if (approvalOption_View!=null) {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", approvalOption_View));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ERROR", "Not found", approvalOption_View));
		}
		
	}
	
}
