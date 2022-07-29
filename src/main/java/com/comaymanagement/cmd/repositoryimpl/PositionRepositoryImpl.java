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

import com.comaymanagement.cmd.entity.Department;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.repository.IPositionRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class PositionRepositoryImpl implements IPositionRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(PositionRepositoryImpl.class);
	
	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public List<PositionModel> findAllByRoleId(Integer roleId) {
		StringBuilder hql = new StringBuilder("FROM positions WHERE role_id = :roleId");
		List <PositionModel> positionModelList = null;
		List <Position> positions = new ArrayList<Position>();

		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("roleId", roleId);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Position po = (Position) obj;
				positions.add(po);
			}
			positionModelList = new ArrayList<PositionModel>();
			for(Position po : positions) {
				PositionModel positionModel = new PositionModel();
				positionModel.setId(po.getId());
				positionModel.setName(po.getName());
				positionModel.setIsManager(po.getIsManager());
				Department department = po.getDepartment();
				DepartmentModel departmentModel = new DepartmentModel();
				departmentModel.setId(department.getId());
				departmentModel.setName(department.getName());
				positionModel.setDepartment(departmentModel);
//				positionModel.setRole(po.getRole());
				positionModelList.add(positionModel);
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return positionModelList;
	}

	@Override
	public Integer CountTotalItem() {
		Integer count = null;
		StringBuilder hql = new StringBuilder("SELECT COUNT(*) FROM positions");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			@SuppressWarnings("rawtypes")
			List list = query.getResultList();
			count = Integer.valueOf(list.get(0).toString());
		}catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return count;
	}

	@Override
	public Integer add(Position p) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Integer id = (Integer) session.save(p);
			return id;
		} catch (Exception e) {
			LOGGER.error("Error has occured in PositionRepositoryImpl at save() ", e);
			return -1;
		}
		
	}
	@Override
	public Integer edit(Position p) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(p);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at edit() ", e);
			return 0;
		}
	}
	
	@Override
	public List<PositionModel> findAllByDepartmentId(Integer depId) {
		StringBuilder hql = new StringBuilder("FROM positions pos WHERE pos.department.id = :depId");
		List <PositionModel> positionModelList = new ArrayList<PositionModel>();
		List <Position> positions = new ArrayList<Position>();

		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("depId", depId);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Position po = (Position) obj;
				positions.add(po);
			}
			for(Position po : positions) {
				PositionModel positionModel = new PositionModel();
				positionModel.setId(po.getId());
				positionModel.setName(po.getName());
				positionModel.setIsManager(po.getIsManager());
				positionModel.setRole(po.getRole());
				positionModelList.add(positionModel);
			}
			
		} catch (Exception e) {
			LOGGER.error("Error has occured in findAllByDepartmentId() ", e);
		}
		return positionModelList;
	}
	@Override
	public List<PositionModel> findAllByTeamId(Integer teamId) {
		StringBuilder hql = new StringBuilder("FROM positions pos WHERE pos.team.id = :teamId");
		List <PositionModel> positionModelList = new ArrayList<PositionModel>();
		List <Position> positions = new ArrayList<Position>();

		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("teamId", teamId);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Position po = (Position) obj;
				positions.add(po);
			}
			for(Position po : positions) {
				PositionModel positionModel = new PositionModel();
				positionModel.setId(po.getId());
				positionModel.setName(po.getName());
				positionModel.setIsManager(po.getIsManager());
				positionModel.setRole(po.getRole());
				positionModelList.add(positionModel);
			}
			
		} catch (Exception e) {
			LOGGER.error("Error has occured in findAllByDepartmentId() ", e);
		}
		return positionModelList;
	}
	public String delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Position pos = new Position();
			pos = session.find(Position.class, id);
			session.remove(pos);
			return "1";
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return "0";
		}

	}
	public Position findById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "FROM positions pos WHERE pos.id = :id";
		Position pos = null;
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
			pos = (Position) query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Error has occured in checkEmployeeIdExisted() ", e);
		}
		return pos;
	}
	
	@Override
	public List<Position> findAllByDepId(Integer depId) {
		StringBuilder hql = new StringBuilder("FROM positions pos WHERE pos.department.id = :depId");
		List <Position> positions = new ArrayList<Position>();

		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("depId", depId);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object obj = (Object) it.next();
				Position po = (Position) obj;
				positions.add(po);
			}
			return positions;
		} catch (Exception e) {
			LOGGER.error("Error has occured in findAllByDepartmentId() ", e);
			return null;
		}
	}
	
	@Override
	public List<Position> findAllByEmployeeId(Integer empId) {
		StringBuilder hql =  new StringBuilder();
		List <Position> positions = new ArrayList<Position>();
		hql.append("from positions pos ");
		hql.append("inner join pos.employees emp ");
		hql.append("where emp.id = :empId");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("empId", empId);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				Position po = (Position) obj[0];
				positions.add(po);
			}
			return positions;
		} catch (Exception e) {
			LOGGER.error("Error has occured in findAllByEmployeeId() ", e);
			return null;
		}
	}
	public PositionModel toModel(Position position) {
		PositionModel positionModel = new PositionModel();
		positionModel.setId(position.getId());
		positionModel.setName(position.getName());
		positionModel.setIsManager(position.getIsManager());
		Department department = position.getDepartment();
		DepartmentModel departmentModel = new DepartmentModel();
		departmentModel.setId(department.getId());
		departmentModel.setName(department.getName());
		positionModel.setDepartment(departmentModel);
//		positionModel.setRole(po.getRole());
		return positionModel;
	}
}
