package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.Permission;
import com.comaymanagement.cmd.model.PermissionModel;
import com.comaymanagement.cmd.repository.IPermissionRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class PermissionRepositoryImpl implements IPermissionRepository{
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public List<PermissionModel> findAll() {
		Session session = sessionFactory.getCurrentSession();
		List<PermissionModel> permissionModelList = new ArrayList<>();
		StringBuilder hql = new StringBuilder();
		hql.append("FROM permissions per order by per.id asc");
		Query query = session.createQuery(hql.toString());
		for(Iterator it = query.getResultList().iterator();it.hasNext();) {
			Permission permission = (Permission) it.next();
			PermissionModel permissionModel = new PermissionModel();
			permissionModel.setId(permission.getId());
			permissionModel.setName(permission.getName());
			permissionModel.setLabel(permission.getLabel());
			permissionModelList.add(permissionModel);
		}
		return permissionModelList;
	}


}
