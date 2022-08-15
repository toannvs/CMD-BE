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

}
