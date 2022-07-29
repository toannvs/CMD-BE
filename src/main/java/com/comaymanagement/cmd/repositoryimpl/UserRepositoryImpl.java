package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.model.RoleDetailModel;
import com.comaymanagement.cmd.model.UserModel;
import com.comaymanagement.cmd.repository.UserRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class UserRepositoryImpl implements UserRepository{
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	RoleRepositoryImpl roleRepository;
	@Override
	public UserModel findByUsername(String username) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		hql.append("FROM employees as emp where emp.username = :username");
		UserModel user = null;
		List<RoleDetailModel> roleDetailModels = null;
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("username", username);
			List ob = query.getResultList();
			if(ob.size() > 0) {
				Employee employee = (Employee) ob.get(0);
				user = new UserModel();
				roleDetailModels = new ArrayList<>();
				user.setId(employee.getId());
				user.setUsername(employee.getUsername());
				user.setEmail(employee.getEmail());
				user.setPassword(employee.getPassword());
				for(Position position : employee.getPositions()) {
					Role role = position.getRole();
					RoleDetailModel roleModelDetail = roleRepository.findRoleDetailByRoleId(role.getId());
					roleDetailModels.add(roleModelDetail);
				}
				user.setRoles(roleDetailModels);
			}
			
			return user;
		} catch (Exception e) {
			LOGGER.error("Have error at findByUsername(): ", e);
			return null;
		}
	}

	@Override
	public Boolean existsByUsername(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean existsByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

}
