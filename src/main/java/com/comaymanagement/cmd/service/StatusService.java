package com.comaymanagement.cmd.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Status;
import com.comaymanagement.cmd.repositoryimpl.StatusRepositotyImpl;

import net.bytebuddy.asm.Advice.This;

@Service
@Transactional(rollbackFor = Exception.class)
public class StatusService {
	
	@Autowired
	StatusRepositotyImpl statusRepositoty;

	private static final Logger LOOGER = LoggerFactory.getLogger(This.class); 
	public ResponseEntity<Object> findAllForTask() {
		List<Status> statuses = null;
		try {
			statuses = statusRepositoty.findAllForTask();
			
			if ( statuses != null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "SUCCESSFULLY: ", statuses));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "NOT FOUND", ""));
			}
		} catch (Exception e) {
			LOOGER.error(e.getMessage());
		}
		return null;

	}
	public ResponseEntity<Object> findAllForProposal() {
		List<Status> statuses = null;
		try {
			statuses = statusRepositoty.findAllForProposal();
			
			if ( statuses != null) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "SUCCESSFULLY: ", statuses));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", "NOT FOUND", statuses));
			}
		} catch (Exception e) {
			LOOGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("ERROR", "NOT FOUND", statuses));
		}
		
	}

	
}
