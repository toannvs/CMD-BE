package com.comaymanagement.cmd.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.constant.Message;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApprovalStepDetailService {
	@Autowired
	Message message;
	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalStepDetailService.class);

}
