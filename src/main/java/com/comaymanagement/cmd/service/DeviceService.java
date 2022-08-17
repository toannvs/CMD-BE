package com.comaymanagement.cmd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.Device;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.model.OptionModel;
import com.comaymanagement.cmd.repositoryimpl.DeviceRepositoryImpl;

@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceService {
	@Autowired
	DeviceRepositoryImpl deviceRepository;
	public ResponseEntity<Object> findAll() {
		List<Device> devices = new ArrayList<Device>();;
		devices = deviceRepository.findAll();
		
		if(devices != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseObject("OK", "OK", devices));
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ResponseObject("ERROR", "Có lỗi xảy ra", devices));
	}
}
