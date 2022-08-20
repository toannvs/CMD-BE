package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.Option;
import com.comaymanagement.cmd.entity.Permission;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.RoleDetail;
import com.comaymanagement.cmd.model.OptionModel;
import com.comaymanagement.cmd.model.PermissionModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.RoleDetailModel;
import com.comaymanagement.cmd.model.RoleModel;
import com.comaymanagement.cmd.repository.IRoleRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class RoleRepositoryImpl implements IRoleRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleRepositoryImpl.class);

	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	PositionRepositoryImpl positionRepositoryImpl;
	
	@Autowired
	PermissionRepositoryImpl permissionRepository;

	@Autowired
	OptionRepositoryImpl optionRepository;
	
	@Override
	public List<RoleModel> findAll(String name, String sort, String order, Integer limit, Integer offset) {
		List<Role> roles = new ArrayList<Role>();
		List<RoleModel> roleModelList = new ArrayList<RoleModel>();
		StringBuilder hql = new StringBuilder();
		hql.append("FROM roles r ");
		hql.append("WHERE r.name like CONCAT('%',:name,'%') ");
		hql.append("ORDER BY " + sort + " " + order);
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("name", name);
			
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				Object obj  = (Object) it.next();
				Role role = (Role) obj;
				roles.add(role);
			}
			
			for(Role role : roles) {
				RoleModel roleModel = new RoleModel();
				roleModel.setId(role.getId());
				roleModel.setName(role.getName());
				List<PositionModel> positionModelList = positionRepositoryImpl.findAllByRoleId(role.getId());
				roleModel.setPositions(positionModelList);	
				roleModelList.add(roleModel);
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}

		return roleModelList;
	}
	
	@Override
	public Integer countAllPaging(String name, String sort, String order, Integer limit, Integer offset) {
		Integer count = 0;
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT COUNT(*) FROM roles r ");
		hql.append("WHERE r.name like CONCAT('%',:name,'%') ");
		hql.append("ORDER BY " + sort + " " + order);
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("name", name);
			query.setFirstResult(offset);
			query.setMaxResults(limit);
			count = Integer.valueOf(query.uniqueResult().toString() );
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return count;
	}

	@Override
	public RoleDetailModel findRoleDetailByRoleId(Integer roleId) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		hql.append("FROM role_details as rd ");
		hql.append("INNER JOIN roles as r on rd.roleId = r.id ");
		hql.append("INNER JOIN options as op on rd.optionId = op.id ");
		hql.append("INNER JOIN permissions as per on rd.permissionId = per.id ");
		hql.append("WHERE r.id = :roleId ORDER BY rd.optionId asc");
		
		Role role = new Role();
		RoleDetailModel roleDetailModel = null;
		Set<Option> options = new LinkedHashSet<Option>();
		List<Permission> permissions = new ArrayList<>();
		List<Integer> optionIdOrigin = new ArrayList<>();
		List<PermissionModel> permissionModelList = permissionRepository.findAll();
		List<OptionModel> optionsModelList = optionRepository.findAll();
		for(OptionModel optionModel : optionsModelList) {
			optionModel.setPermissions(permissionModelList);
		}
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("roleId", roleId);
			Object[] obTMP = (Object[]) query.getResultList().iterator().next();
			role = (Role) obTMP[1];
			for(Iterator it = query.getResultList().iterator();it.hasNext();) {
				Object[] ob = (Object[]) it.next();
				RoleDetail roleDetail = (RoleDetail) ob[0];
				Option option = (Option) ob[2];
				Permission permission = (Permission) ob[3];
				optionIdOrigin.add(roleDetail.getOptionId());
				options.add(option);
				permissions.add(permission);
			}
			
			// Count to add permission to option
			
			for(Option op : options) {
				int countCurrentId = 0;
				Iterator<Integer> opOrigin = optionIdOrigin.iterator();
				Iterator<Permission> itePermission = permissions.iterator();
				// Count permission record at optionId i
				while (opOrigin.hasNext()) {
				   Integer value = opOrigin.next(); // must be called before you can call i.remove()
				   if(op.getId() == value) {
						countCurrentId++;
						opOrigin.remove();
					}
				}
				int k = 0;
				permissionModelList = permissionRepository.findAll();
				while(itePermission.hasNext() && k < countCurrentId) {
					Permission per = itePermission.next();
					// Permission must sort by id asc
					permissionModelList.get(per.getId()-1).setSelected(true);;
					itePermission.remove();
					k++;
				}
				optionsModelList.get(op.getId()-1).setPermissions(permissionModelList);
			}
			roleDetailModel = new RoleDetailModel();
			roleDetailModel.setId(role.getId());
			roleDetailModel.setName(role.getName());
			roleDetailModel.setOptions(optionsModelList);
		} catch (Exception e) {
			LOGGER.error("Error has occured at findAllRoleDetailByRoleId ", e);
		} 
		return roleDetailModel;
	}
	
	@Override
	public Role findById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Role role = null;
		StringBuilder hql = new StringBuilder();
		hql.append("FROM roles r where r.id = :id");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
			role = (Role) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Have error at findById(): ",e);
		}
		return role;
	}
	
	@Override
	public Integer add(Role role){
		Session session = sessionFactory.getCurrentSession();
		try {
			return Integer.parseInt(session.save(role).toString());
		} catch (Exception e) {
			LOGGER.error("Error has occured in addEmployee() ", e);
			return -1;
		}

	}

	@Override
	public Integer edit(Role role) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(role);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured in at edit() ", e);
			return -1;
		}
	}
	
	@Override
	public Integer delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Role role = new Role();
			role = session.find(Role.class, id);
			session.remove(role);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return -1;
		}
		
	}
	
	@Override
	public List<Integer> findAllRoleIdByEmpId(Integer empId){
		Session session = sessionFactory.getCurrentSession();
		List<Integer> roleIds = null;
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT pos.role.id as id FROM employees emp ");
		hql.append("inner join emp.positions as pos ");
		hql.append("WHERE emp.id = :empId");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("empId", empId);
			if(query.getResultList().size() > 0 ) {
				roleIds = new ArrayList<>();
				roleIds =  query.getResultList();
			}
		} catch (Exception e) {
			LOGGER.error("Have error at findAllByEmpId(): ",e);
		}
		return roleIds;
	}
	
	public RoleModel toModel(Role role) {
		RoleModel roleModel = new RoleModel();
		roleModel.setId(role.getId());
		roleModel.setName(role.getName());
		List<PositionModel> positionModelList = positionRepositoryImpl.findAllByRoleId(role.getId());
		roleModel.setPositions(positionModelList);
		return roleModel;
	}
	
}
