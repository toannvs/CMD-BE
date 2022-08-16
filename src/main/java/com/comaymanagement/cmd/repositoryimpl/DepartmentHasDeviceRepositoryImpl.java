package com.comaymanagement.cmd.repositoryimpl;

import java.util.Iterator;
import java.util.LinkedHashSet;
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
import com.comaymanagement.cmd.entity.DepartmentHasDevice;
import com.comaymanagement.cmd.entity.Device;
import com.comaymanagement.cmd.model.DeviceModel;
import com.comaymanagement.cmd.repository.IDepartmentHasDeviceRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class DepartmentHasDeviceRepositoryImpl implements IDepartmentHasDeviceRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentHasDeviceRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	
	
	public Set<Department> findAllDeviceByDepartmentName(String name) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		hql.append("from departments dep ");
		hql.append("inner join dep.departmentHasDevices depHasDe");
		if(name!=null) {
			hql.append( "where device.name like :name ");
		}
		Set<Department> deppartments = new LinkedHashSet<Department>();
		try {
			Query query = session.createQuery(hql.toString());
			if(name!=null) {
				query.setParameter("name", "%" + name + "%");
			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] ob = (Object[])it.next();
				Department dep = (Department) ob[0];
				dep.getDepartmentHasDevices().size();
				deppartments.add(dep);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at findAll() ", e);
			return null;
		}
		return deppartments;
	}


	@Override
	public DeviceModel add(DepartmentHasDevice departmentHasDevice) {
		DeviceModel deviceModel = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer result = (Integer) session.save(departmentHasDevice);
			if(result > 0) {
				deviceModel = new DeviceModel();
				deviceModel.setId(result);
				deviceModel.setDescription(departmentHasDevice.getDescription());
				deviceModel.setName(departmentHasDevice.getDevice().getName());
				deviceModel.setIsActive(departmentHasDevice.getIsActive());
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at add() ", e);
		}
		return deviceModel;
	}


	@Override
	public Device findById(Integer id) {
		Device device = null;
		StringBuilder hql = new StringBuilder("FROM devices AS dv ");
		hql.append("WHERE dv.id = " + id);
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			device = (Device) query.getSingleResult();

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return device;
	}


	@Override
	public DeviceModel edit(DepartmentHasDevice departmentHasDevice) {
		DeviceModel deviceModel = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			session.update(departmentHasDevice);
			deviceModel = new DeviceModel();
			deviceModel.setId(departmentHasDevice.getId());
			deviceModel.setName(departmentHasDevice.getDevice().getName());
			deviceModel.setDescription(departmentHasDevice.getDescription());
			deviceModel.setIsActive(departmentHasDevice.getIsActive());
			return deviceModel;

		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at add() ", e);
		}
		return null;
	}


	@Override
	public DepartmentHasDevice findDepartmentHasDeviceById(Integer id) {
		DepartmentHasDevice deviceDepartmentHasDevice = null;
		StringBuilder hql = new StringBuilder("FROM departments_devices AS dv ");
		hql.append("WHERE dv.id = " + id);
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			deviceDepartmentHasDevice = (DepartmentHasDevice) query.getSingleResult();

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return deviceDepartmentHasDevice;
	}


	@Override
	public Boolean delete(DepartmentHasDevice departmentHasDevice) {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.delete(departmentHasDevice);
			return true;

		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at add() ", e);
		}
		return false;
	}

}
