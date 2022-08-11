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

import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.Team;
import com.comaymanagement.cmd.model.EmployeeModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.TeamModel;
import com.comaymanagement.cmd.repository.ITeamRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class TeamRepositoryImpl implements ITeamRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	EmployeeRepositoryImpl employeeRepository;

	
	@Override
	@Transactional
	public Set<TeamModel> findAll(String name) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "from teams team where team.name like CONCAT('%',:name,'%')";
		Set<TeamModel> teamModelSet = new LinkedHashSet<TeamModel>();
		Set<Team> teamSetTMP = new LinkedHashSet<Team>();
		
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("name", name);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Team tmp= (Team) it.next();
				teamSetTMP.add(tmp);
			}
			for(Team t : teamSetTMP) {
				List<EmployeeModel> employeeModels = new ArrayList<>();
				TeamModel teamModel = new TeamModel();
				List<PositionModel> positionModelList = new ArrayList<>();
				teamModel.setId(t.getId());
				teamModel.setCode(t.getCode());
				teamModel.setName(t.getName());
				teamModel.setDescription(t.getDescription());
				for (Position pos : t.getPositions()) {
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
				for(Employee emp : t.getEmployees()) {
					employeeModels.add(employeeRepository.toModelForTeam(emp,t.getId()));
				}
				teamModel.setPositions(positionModelList);
				teamModel.setHeadPosition(t.getHeadPosition());
				teamModel.setEmployees(employeeModels);
				teamModelSet.add(teamModel);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in DepartmentRepositoryImpl at findAll() ", e);
			return null;
		}
		return teamModelSet;
	}

	@Transactional
	public boolean isExisted(Integer id, String code) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "from teams team where team.code = :code and team.id != :id";
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("code", code);
			query.setParameter("id", id);
			List list = query.getResultList();
			if (list.size()>0) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in at isExisted() ", e);
		}
		return false;
	}

	@Transactional
	@Override
	public Integer add(Team team) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Integer id = (Integer) session.save(team);
			return id;
		} catch (Exception e) {
			LOGGER.error("Error has occured at add() ", e);
		}
		return -1;
	}

	@Override
	public Integer edit(Team team) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(team);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured at edit() ", e);
			return 0;
		}
	}

	@Override
	public String delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Team team = new Team();
			team = session.find(Team.class, id);
			session.remove(team);
			return "1";
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return "0";
		}

	}

	@Override
	public Team findById(Integer id) {

		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		Team team = null;
		hql.append("from teams team where team.id = " + id);
		try {
			Query query = session.createQuery(hql.toString());
			team = (Team) query.getSingleResult();
			return team;
		} catch (Exception e) {
			LOGGER.error("Error has occured at findById() ", e);
			return null;
		}
	}

	@Override
	public Team findByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		Team team = null;
		hql.append("FROM teams team WHERE team.name= :name");
		
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("name", name);
			LOGGER.info(hql.toString());
			team =  (Team)query.getSingleResult();
		} catch (Exception e) {
			LOGGER.error("Error has occured at findByName() ", e);
		}
		return team;
	}
	public TeamModel toModel(Team t) {
		TeamModel teamModel = new TeamModel();
		List<PositionModel> positionModelList = new ArrayList<>();
		teamModel.setId(t.getId());
		teamModel.setCode(t.getCode());
		teamModel.setName(t.getName());
		teamModel.setDescription(t.getDescription());
		for (Position pos : t.getPositions()) {
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
		teamModel.setPositions(positionModelList);
		teamModel.setHeadPosition(t.getHeadPosition());
		return teamModel;
	}
}
