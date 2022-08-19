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

import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.repository.IDepartmentRepository;

@Repository
@Transactional(rollbackFor = Exception.class)
public class DepartmentRepositoryImpl implements IDepartmentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;


	
	@Override
	public Set<DepartmentModel> findAll(String name) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "from departments dep where dep.name like CONCAT('%',:name,'%')";
		Set<DepartmentModel> departmentModelSet = new LinkedHashSet<DepartmentModel>();
		Set<Department> depSetTMP = new LinkedHashSet<Department>();
		
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("name", name);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object ob = (Object) it.next();
				Department tmp = (Department) ob;
				depSetTMP.add(tmp);
			}
			for(Department d : depSetTMP) {
				DepartmentModel departmentModel = new DepartmentModel();
				List<PositionModel> positionModelList = new ArrayList<>();
				departmentModel.setId(d.getId());
				departmentModel.setCode(d.getCode());
				departmentModel.setName(d.getName());
				departmentModel.setDescription(d.getDescription());
				departmentModel.setFatherDepartmentId(d.getFatherDepartmentId());
				departmentModel.setLevel(d.getLevel());
				for (Position pos : d.getPositions()) {
					PositionModel positionModel = new PositionModel();
					Role role = new Role();
					role.setId(pos.getRole().getId());
					role.setName(pos.getRole().getName());
					positionModel.setId(pos.getId());
					positionModel.setName(pos.getName());
					positionModel.setIsManager(pos.getIsManager());
					positionModel.setRole(role);
					positionModelList.add(positionModel);
				}
				departmentModel.setPositions(positionModelList);
				departmentModel.setHeadPosition(d.getHeadPosition());
				departmentModelSet.add(departmentModel);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at findAll() ", e);
			return null;
		}
		return departmentModelSet;
	}

	public boolean isExisted(Integer id, String code) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "from departments dep where dep.code = :code and dep.id != :id";
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("code", code);
			query.setParameter("id", id);
			List list = query.getResultList();
			if (list.size()>0) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at isExisted() ", e);
		}
		return false;
	}

	@Override
	public Integer add(Department dep) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Integer id = (Integer) session.save(dep);
			return id;
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at add() ", e);
		}
		return -1;
	}

	@Override
	public Integer edit(Department dep) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(dep);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at edit() ", e);
			return 0;
		}
	}
	
	@Override
	public String delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Department dep = new Department();
			dep = session.find(Department.class, id);
			session.remove(dep);
			return "1";
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return "0";
		}

	}

	@Override
	@javax.transaction.Transactional
	public Department findById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		hql.append("from departments dep ");
		hql.append("where dep.id = " + id);
		try {
			Query query = session.createQuery(hql.toString());
			Department e = (Department) query.getSingleResult();
			return e;
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return null;
		}
		
	}
	@Override
	public Department findByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		Department department = null;
		hql.append("FROM departments dep WHERE dep.name= :name");
		System.out.println(name);
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("name", name);
			LOGGER.info(hql.toString());
			department =  (Department)query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl findByName() ", e);
		}
		return department;
	}
	@Override
	public List<Department> findAllByEmployeeId(Integer empId) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		List<Department> departments = new ArrayList<>();
		hql.append("from departments dep ");
		hql.append("inner join dep.employees as emp ");
		hql.append("where emp.id = :empId");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("empId", empId);
			LOGGER.info(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] ob = (Object[]) it.next();
				Department tmp = (Department) ob[0];
				departments.add(tmp);
			}
			return departments;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
	public DepartmentModel toModel(Department d) {
		DepartmentModel departmentModel = new DepartmentModel();
		List<PositionModel> positionModelList = new ArrayList<>();
		departmentModel.setId(d.getId());
		departmentModel.setCode(d.getCode());
		departmentModel.setName(d.getName());
		departmentModel.setDescription(d.getDescription());
		departmentModel.setFatherDepartmentId(d.getFatherDepartmentId());
		departmentModel.setLevel(d.getLevel());
		for (Position pos : d.getPositions()) {
			PositionModel positionModel = new PositionModel();
			Role role = new Role();
			role.setId(pos.getRole().getId());
			role.setName(pos.getRole().getName());
			positionModel.setId(pos.getId());
			positionModel.setName(pos.getName());
			positionModel.setIsManager(pos.getIsManager());
			positionModel.setRole(role);
			positionModelList.add(positionModel);
		}
		departmentModel.setPositions(positionModelList);
		departmentModel.setHeadPosition(d.getHeadPosition());
		return departmentModel;
	}
	
	public List<Department> findAllChild(Integer fatherId) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		List<Department> departments = new ArrayList<>();
		hql.append("from departments dep ");
		hql.append("where dep.fatherDepartmentId = :fatherId");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("fatherId", fatherId);
			LOGGER.info(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Department tmp= (Department) it.next();
				departments.add(tmp);
			}
			return departments;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	
}
