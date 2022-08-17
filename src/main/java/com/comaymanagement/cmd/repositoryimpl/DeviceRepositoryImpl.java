package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Device;
import com.comaymanagement.cmd.repository.IDeviceRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class DeviceRepositoryImpl implements IDeviceRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	public List<Device> findAll() {
		Session session = sessionFactory.getCurrentSession();
		List<Device> devices = new ArrayList<>();
		StringBuilder hql = new StringBuilder();
		hql.append("from devices");
		try {
			Query query = session.createQuery(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Device device = (Device)it.next();
				devices.add(device);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at findAll() ", e);
			return null;
		}
		return devices;
	}
}
