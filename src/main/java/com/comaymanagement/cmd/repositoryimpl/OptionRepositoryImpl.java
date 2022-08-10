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

import com.comaymanagement.cmd.entity.Option;
import com.comaymanagement.cmd.entity.Permission;
import com.comaymanagement.cmd.entity.RoleDetail;
import com.comaymanagement.cmd.model.OptionModel;
import com.comaymanagement.cmd.model.PermissionModel;
import com.comaymanagement.cmd.repository.IOptionRepository;
@Repository
public class OptionRepositoryImpl implements IOptionRepository{
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	PermissionRepositoryImpl permissionRepository;
	
	@Override
	public List<OptionModel> findAll() {
		Session session = sessionFactory.getCurrentSession();
		List<OptionModel> optionModelList = new ArrayList<>();
		StringBuilder hql = new StringBuilder(); 
		hql.append("FROM options op order by op.id asc");
		try {
			Query query = session.createQuery(hql.toString()) ;
			for(Iterator it = query.getResultList().iterator(); it.hasNext();) {
				 Option option = (Option) it.next();
				 OptionModel optionModel = new OptionModel();
				 optionModel.setId(option.getId());
				 optionModel.setName(option.getName());
				 optionModel.setLabel(option.getLabel());
				 optionModelList.add(optionModel);
			}
		} catch (Exception e) {
			LOGGER.error("ERROR at findAll(): ", e);
		}
		return optionModelList;
	}
	@Override
	public Set<Option> findByRoleId(Integer roleId) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		Set<Option> options = new LinkedHashSet<>();
		hql.append("FROM role_details as rd ");
		hql.append("INNER JOIN rd.role as r ");
		hql.append("INNER JOIN options as op on rd.optionId = op.id ");
		hql.append("INNER JOIN permissions as per on rd.permissionId = per.id ");
		hql.append("WHERE r.id = :roleId ORDER BY rd.optionId asc");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("roleId", roleId);
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				Object[] ob = (Object[]) it.next();
				Option option = (Option) ob[2];
				options.add(option);
			}
			
			return options;
		} catch (Exception e) {
			LOGGER.error("Have error at findByRoleId(): ", e);
			return null;
		}
			
	}
	public List<OptionModel> findAllWithPermissionDefault(){
		List<PermissionModel> permissionModelList = permissionRepository.findAll();
		List<OptionModel> optionsModelList = this.findAll();
		for(OptionModel optionModel : optionsModelList) {
			optionModel.setPermissions(permissionModelList);
		}

		return optionsModelList;
	}

}
