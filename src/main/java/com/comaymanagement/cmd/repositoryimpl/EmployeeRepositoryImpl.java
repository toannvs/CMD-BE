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
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Position;
import com.comaymanagement.cmd.entity.Role;
import com.comaymanagement.cmd.entity.Team;
import com.comaymanagement.cmd.model.DepartmentModel;
import com.comaymanagement.cmd.model.EmployeeModel;
import com.comaymanagement.cmd.model.PositionModel;
import com.comaymanagement.cmd.model.TeamModel;
import com.comaymanagement.cmd.model.UserModel;
import com.comaymanagement.cmd.repository.IEmployeeRepository;

@Repository
@Transactional(rollbackFor = Exception.class)
public class EmployeeRepositoryImpl implements IEmployeeRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public List<Employee> findByActiveFlag(Boolean activeFlag) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// find all employee with position in department
	@Override
	public Set<EmployeeModel> findAll(String name, String dob, String email, String phone, List<Integer> departmentIds,
			List<Integer> positionIds, String sort, String order, Integer limit, Integer offset) {
		StringBuilder hql = new StringBuilder();
		hql.append("from employees emp ");
		hql.append("inner join emp.positions as pos inner join emp.departments as dep ");
		hql.append("where pos.team.id is null ");
		hql.append("and pos.department.id is not null ");
		hql.append("and emp.activeFlag = true ");
		if (!name.equals("")) {
			hql.append("and emp.name like CONCAT('%',:name,'%') ");
		}
		if (!dob.equals("")) {
			hql.append("and emp.dateOfBirth like CONCAT('%',:dob,'%') ");
		}
		if (!email.equals("")) {
			hql.append("and emp.email like CONCAT('%',:email,'%') ");
		}
		if (!phone.equals("")) {
			hql.append("and emp.phoneNumber like CONCAT('%',:phone,'%') ");
		}
		if (departmentIds.size() > 0) {
			hql.append("and dep.id IN (:departmentIds) ");
		}
		if (positionIds.size() > 0) {
			hql.append("and pos.id IN (:positionIds) ");

		}

		hql.append("order by " + sort + " " + order);
		Session session = this.sessionFactory.getCurrentSession();
		Set<Employee> empSet = new LinkedHashSet<>();
		Set<EmployeeModel> employeeModelSet = new LinkedHashSet<>();
		try {
			Query query = session.createQuery(hql.toString());
			if (!name.equals("")) {
				query.setParameter("name","\\" +  name);
			}
			if (!dob.equals("")) {
				query.setParameter("dob", dob);
			}
			if (!email.equals("")) {
				query.setParameter("email", email);
			}
			if (!phone.equals("")) {
				query.setParameter("phone", phone);
			}
			if (departmentIds.size() > 0) {
				query.setParameter("departmentIds", departmentIds);
			}
			if (positionIds.size() > 0) {
				query.setParameter("positionIds", positionIds);

			}
			query.setFirstResult(offset);
			if(limit>0) {
				query.setMaxResults(limit);
				
			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] ob = (Object[]) it.next();
				Employee e = (Employee) ob[0];
				empSet.add(e);
			}

			for (Employee e : empSet) {
				EmployeeModel employeeModel = new EmployeeModel();
				List<PositionModel> positionModelList = new ArrayList<>();
				List<DepartmentModel> departmentModelList = new ArrayList<>();
				List<TeamModel> teamModelList = new ArrayList<>();
				// Add team list
				// Add position list
				// Add department list
				for (Position p : e.getPositions()) {
					PositionModel positionModel = new PositionModel();

					Role role = new Role();
					role.setId(p.getRole().getId());
					role.setName(p.getRole().getName());
					positionModel.setId(p.getId());
					positionModel.setName(p.getName());
					positionModel.setIsManager(p.getIsManager());
					positionModel.setRole(role);
					if (p.getDepartment() != null && p.getTeam() == null) {
						Department department = p.getDepartment();
						DepartmentModel departmentModel = new DepartmentModel();
						departmentModel.setId(department.getId());
						departmentModel.setCode(department.getCode());
						departmentModel.setName(department.getName());
						departmentModel.setFatherDepartmentId(department.getFatherDepartmentId());
						departmentModel.setHeadPosition(department.getHeadPosition());
						departmentModel.setDescription(department.getDescription());
						departmentModel.setLevel(department.getLevel());
						departmentModel.setPosition(positionModel);
						departmentModelList.add(departmentModel);
					} else if (p.getDepartment() == null && p.getTeam() != null) {
						Team team = p.getTeam();
						TeamModel teamModel = new TeamModel();
						teamModel.setId(team.getId());
						teamModel.setCode(team.getCode());
						teamModel.setName(team.getName());
						teamModel.setDescription(team.getDescription());
						teamModel.setHeadPosition(team.getHeadPosition());
						teamModel.setPosition(positionModel);
						teamModelList.add(teamModel);
					}
				}
				UserModel user = new UserModel();
				user.setUsername(e.getUsername());
				user.setEnableLogin(e.isEnableLogin());
				employeeModel.setId(e.getId());
				employeeModel.setCode(e.getCode());
				employeeModel.setName(e.getName());
				employeeModel.setAvatar(e.getAvatar());
				employeeModel.setGender(e.getGender());
				employeeModel.setDateOfBirth(e.getDateOfBirth());
				employeeModel.setEmail(e.getEmail());
				employeeModel.setPhoneNumber(e.getPhoneNumber());
				employeeModel.setActive(e.isActive());
				employeeModel.setCreateDate(e.getCreateDate());
				employeeModel.setDepartments(departmentModelList);
				;
				employeeModel.setPositions(positionModelList);
				employeeModel.setUser(user);
				employeeModel.setCreateDate(e.getCreateDate());
				employeeModel.setModifyDate(e.getModifyDate());
				employeeModel.setCreateBy(e.getCreateBy());
				employeeModel.setModifyBy(e.getModifyBy());
				employeeModel.setTeams(teamModelList);
				employeeModelSet.add(employeeModel);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in employeePaging() ", e);

		}

		return employeeModelSet;
	}

	public boolean checkEmployeeCodeExisted(Integer id, String code) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "select count(*) from employees emp where emp.code = :code and emp.id != :id";
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("code", code);
			query.setParameter("id", id);
			List list = query.getResultList();
			Integer count = Integer.valueOf(list.get(0).toString());
			if (count > 0) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in checkEmployeeIdExisted() ", e);
		}

		return false;
	}
	public boolean checkEmployeeUserNameExisted(Integer id, String username) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "select count(*) from employees emp where emp.username = :username and emp.id != :id";
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("username", username);
			query.setParameter("id", id);
			List list = query.getResultList();
			Integer count = Integer.valueOf(list.get(0).toString());
			if (count > 0) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in checkEmployeeUserNameExisted() ", e);
		}
		
		return false;
	}

	@Override
	public Integer add(Employee emp) {
		Session session = sessionFactory.getCurrentSession();
		try {
			return Integer.parseInt(session.save(emp).toString());
		} catch (Exception e) {
			LOGGER.error("Error has occured in addEmployee() ", e);
			return -1;
		}

	}

	@Override
	public Integer edit(Employee emp) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(emp);
			return 1;
		} catch (Exception e) {
			LOGGER.error("Error has occured in editEmployee() ", e);
			return 0;
		}
	}

	@Transactional
	public String delete(Employee emp) {
		try {
			return String.valueOf(edit(emp));
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return "0";
		}

	}

	@Override
	public Integer countAllPaging(String name, String dob, String email, String phone, List<Integer> departmentIds,
			List<Integer> positionIds, String sort, String order, Integer offset, Integer limit) {
		Set<Employee> employeeSet = new LinkedHashSet<>();
		StringBuilder hql = new StringBuilder();
		hql.append("from ");
		hql.append("employees emp ");
		hql.append("inner join emp.positions as pos inner join emp.departments as dep ");
		hql.append("where pos.team.id is null ");
		hql.append("and pos.department.id is not null ");
		hql.append("and emp.activeFlag = true ");
		if (!name.equals("")) {
			hql.append("and emp.name like CONCAT('%',:name,'%') ");
		}
		if (!dob.equals("")) {
			hql.append("and emp.dateOfBirth like CONCAT('%',:dob,'%') ");
		}
		if (!email.equals("")) {
			hql.append("and emp.email like CONCAT('%',:email,'%') ");
		}
		if (!phone.equals("")) {
			hql.append("and emp.phoneNumber like CONCAT('%',:phone,'%') ");
		}
		if (departmentIds.size() > 0) {
			hql.append("and dep.id IN (:departmentIds) ");
		}
		if (positionIds.size() > 0) {
			hql.append("and pos.id IN (:positionIds) ");

		}
		
		hql.append("order by " + sort + " " + order);
		Session session = this.sessionFactory.getCurrentSession();
		List<EmployeeModel> employeeModelList = new ArrayList();
		try {
			Query query = session.createQuery(hql.toString());

			if (!name.equals("")) {
				query.setParameter("name", name);
			}
			if (!dob.equals("")) {
				query.setParameter("dob", dob);
			}
			if (!email.equals("")) {
				query.setParameter("email", email);
			}
			if (!phone.equals("")) {
				query.setParameter("phone", phone);
			}
			if (departmentIds.size() > 0) {
				query.setParameter("departmentIds", departmentIds);
			}
			if (positionIds.size() > 0) {
				query.setParameter("positionIds", positionIds);

			}
			if (offset > 0) {
				query.setFirstResult(offset);

			}
			if (limit > 0) {
				query.setMaxResults(limit);
			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] ob = (Object[]) it.next();
				employeeSet.add((Employee) ob[0]);
			}
			Integer countResult = employeeSet.size();
			return countResult;
		} catch (Exception e) {
			LOGGER.error("Error has occured in employeePaging() ", e);

		}

		return 0;
	}


	public Integer countAllPagingIncludeDuplicate(String name, String dob, String email, String phone,
			List<Integer> departmentIds, List<Integer> positionIds, String sort, String order, Integer offset,
			Integer limit) {
		Set<Employee> employeeSet = new LinkedHashSet<>();
		StringBuilder hql = new StringBuilder();
		hql.append("from ");
		hql.append("employees emp ");
		hql.append("inner join emp.positions as pos inner join emp.departments as dep ");
		hql.append("where pos.team.id is null ");
		hql.append("and pos.department.id is not null ");
		hql.append("and emp.activeFlag = true ");
		if(!name.equals("")) {
			hql.append("and emp.name like CONCAT('%',:name,'%') ");
		}
		if(!dob.equals("")) {
			hql.append("and emp.dateOfBirth like CONCAT('%',:dob,'%') ");
		}
		if(!email.equals("")) {
			hql.append("and emp.email like CONCAT('%',:email,'%') ");
		}
		if(!phone.equals("")) {
			hql.append("and emp.phoneNumber like CONCAT('%',:phone,'%') ");
		}
		if(departmentIds.size()>0) {
			hql.append("and dep.id IN (:departmentIds) ");
		}
		if(positionIds.size()>0) {
			hql.append("and pos.id IN (:positionIds) ");

		}
		
		hql.append("order by " + sort + " " + order);
		Session session = this.sessionFactory.getCurrentSession();
		List<EmployeeModel> employeeModelList = new ArrayList();
		try {
			Query query = session.createQuery(hql.toString());
			if(!name.equals("")) {
				query.setParameter("name", name);
			}
			if(!dob.equals("")) {
				query.setParameter("dob", dob);
			}
			if(!email.equals("")) {
				query.setParameter("email", email);
			}
			if(!phone.equals("")) {
				query.setParameter("phone", phone);
			}
			if(departmentIds.size()>0) {
				query.setParameter("departmentIds", departmentIds);
			}
			if(positionIds.size()>0) {
				query.setParameter("positionIds", positionIds);

			}
			if (offset > 0) {
				query.setFirstResult(offset);

			}
			if (limit > 0) {
				query.setMaxResults(limit);
			}
//			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
//				Object[] ob = (Object[]) it.next();
//				employeeSet.add((Employee) ob[0]);
//			}
			Integer countResult = query.getResultList().size();
			return countResult;
		} catch (Exception e) {
			LOGGER.error("Error has occured in employeePaging() ", e);

		}

		return 0;
	}
// Paging with team - start
	public Set<EmployeeModel> findAllTeams(String name, String dob, String email, String phone, List<Integer> teamIds,
			List<Integer> positionIds, String sort, String order, Integer limit, Integer offset) {
		Set<Employee> employeeSet = new LinkedHashSet<>();
		StringBuilder hql = new StringBuilder();
		hql.append("from employees emp ");
		hql.append("inner join emp.positions as pos inner join emp.departments as dep ");
		hql.append("where pos.team.id is not null ");
		hql.append("and pos.department.id is null ");
		hql.append("and emp.activeFlag = true ");
		if (!name.equals("")) {
			hql.append("and emp.name like CONCAT('%',:name,'%') ");
		}
		if (!dob.equals("")) {
			hql.append("and emp.dateOfBirth like CONCAT('%',:dob,'%') ");
		}
		if (!email.equals("")) {
			hql.append("and emp.email like CONCAT('%',:email,'%') ");
		}
		if (!phone.equals("")) {
			hql.append("and emp.phoneNumber like CONCAT('%',:phone,'%') ");
		}
		if (teamIds.size() > 0) {
			hql.append("and pos.team.id IN (:teamIds) ");
		}
		if (positionIds.size() > 0) {
			hql.append("and pos.id IN (:positionIds) ");

		}

		hql.append("order by " + sort + " " + order);
		Session session = this.sessionFactory.getCurrentSession();
		Set<Employee> empSet = new LinkedHashSet<>();
		Set<EmployeeModel> employeeModelSet = new LinkedHashSet<>();
		try {
			Query query = session.createQuery(hql.toString());
			if (!name.equals("")) {
				query.setParameter("name","\\" +  name);
			}
			if (!dob.equals("")) {
				query.setParameter("dob", dob);
			}
			if (!email.equals("")) {
				query.setParameter("email", email);
			}
			if (!phone.equals("")) {
				query.setParameter("phone", phone);
			}
			if (teamIds.size() > 0) {
				query.setParameter("teamIds", teamIds);
			}
			if (positionIds.size() > 0) {
				query.setParameter("positionIds", positionIds);

			}
			query.setFirstResult(offset);
			if(limit>0) {
				query.setMaxResults(limit);
				
			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] ob = (Object[]) it.next();
				Employee e = (Employee) ob[0];
				empSet.add(e);
			}

			for (Employee e : empSet) {
				EmployeeModel employeeModel = new EmployeeModel();
				List<PositionModel> positionModelList = new ArrayList<>();
				List<DepartmentModel> departmentModelList = new ArrayList<>();
				List<TeamModel> teamModelList = new ArrayList<>();
				// Add team list
				// Add position list
				// Add department list
				for (Position p : e.getPositions()) {
					PositionModel positionModel = new PositionModel();

					Role role = new Role();
					role.setId(p.getRole().getId());
					role.setName(p.getRole().getName());
					positionModel.setId(p.getId());
					positionModel.setName(p.getName());
					positionModel.setIsManager(p.getIsManager());
					positionModel.setRole(role);
					if (p.getDepartment() != null && p.getTeam() == null) {
						Department department = p.getDepartment();
						DepartmentModel departmentModel = new DepartmentModel();
						departmentModel.setId(department.getId());
						departmentModel.setCode(department.getCode());
						departmentModel.setName(department.getName());
						departmentModel.setFatherDepartmentId(department.getFatherDepartmentId());
						departmentModel.setHeadPosition(department.getHeadPosition());
						departmentModel.setDescription(department.getDescription());
						departmentModel.setLevel(department.getLevel());
						departmentModel.setPosition(positionModel);
						departmentModelList.add(departmentModel);
					} else if (p.getDepartment() == null && p.getTeam() != null) {
						Team team = p.getTeam();
						TeamModel teamModel = new TeamModel();
						teamModel.setId(team.getId());
						teamModel.setCode(team.getCode());
						teamModel.setName(team.getName());
						teamModel.setDescription(team.getDescription());
						teamModel.setHeadPosition(team.getHeadPosition());
						teamModel.setPosition(positionModel);
						teamModelList.add(teamModel);
					}
				}
				UserModel user = new UserModel();
				user.setUsername(e.getUsername());
				user.setEnableLogin(e.isEnableLogin());
				employeeModel.setId(e.getId());
				employeeModel.setCode(e.getCode());
				employeeModel.setName(e.getName());
				employeeModel.setAvatar(e.getAvatar());
				employeeModel.setGender(e.getGender());
				employeeModel.setDateOfBirth(e.getDateOfBirth());
				employeeModel.setEmail(e.getEmail());
				employeeModel.setPhoneNumber(e.getPhoneNumber());
				employeeModel.setActive(e.isActive());
				employeeModel.setCreateDate(e.getCreateDate());
				employeeModel.setDepartments(departmentModelList);
				;
				employeeModel.setPositions(positionModelList);
				employeeModel.setUser(user);
				employeeModel.setCreateDate(e.getCreateDate());
				employeeModel.setModifyDate(e.getModifyDate());
				employeeModel.setCreateBy(e.getCreateBy());
				employeeModel.setModifyBy(e.getModifyBy());
				employeeModel.setTeams(teamModelList);
				employeeModelSet.add(employeeModel);
			}
		} catch (Exception e) {
			LOGGER.error("Error has occured in employeePaging() ", e);

		}

		return employeeModelSet;
	}
	public Integer countAllPagingTeams(String name, String dob, String email, String phone, List<Integer> teamIds,
			List<Integer> positionIds, String sort, String order, Integer offset, Integer limit) {
		Set<Employee> employeeSet = new LinkedHashSet<>();
		StringBuilder hql = new StringBuilder();
		hql.append("from ");
		hql.append("employees emp ");
		hql.append("inner join emp.positions as pos inner join emp.departments as dep ");
		hql.append("where pos.team.id is not null ");
		hql.append("and pos.department.id is null ");
		hql.append("and emp.activeFlag = true ");
		if (!name.equals("")) {
			hql.append("and emp.name like CONCAT('%',:name,'%') ");
		}
		if (!dob.equals("")) {
			hql.append("and emp.dateOfBirth like CONCAT('%',:dob,'%') ");
		}
		if (!email.equals("")) {
			hql.append("and emp.email like CONCAT('%',:email,'%') ");
		}
		if (!phone.equals("")) {
			hql.append("and emp.phoneNumber like CONCAT('%',:phone,'%') ");
		}
		if (teamIds.size() > 0) {
			hql.append("and pos.team.id IN (:teamIds) ");
		}
		if (positionIds.size() > 0) {
			hql.append("and pos.id IN (:positionIds) ");

		}
		
		hql.append("order by " + sort + " " + order);
		Session session = this.sessionFactory.getCurrentSession();
		List<EmployeeModel> employeeModelList = new ArrayList();
		try {
			Query query = session.createQuery(hql.toString());

			if (!name.equals("")) {
				query.setParameter("name", name);
			}
			if (!dob.equals("")) {
				query.setParameter("dob", dob);
			}
			if (!email.equals("")) {
				query.setParameter("email", email);
			}
			if (!phone.equals("")) {
				query.setParameter("phone", phone);
			}
			if (teamIds.size() > 0) {
				query.setParameter("teamIds", teamIds);
			}
			if (positionIds.size() > 0) {
				query.setParameter("positionIds", positionIds);

			}
			if (offset > 0) {
				query.setFirstResult(offset);

			}
			if (limit > 0) {
				query.setMaxResults(limit);
			}
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] ob = (Object[]) it.next();
				employeeSet.add((Employee) ob[0]);
			}
			Integer countResult = employeeSet.size();
			return countResult;
		} catch (Exception e) {
			LOGGER.error("Error has occured in employeePaging() ", e);

		}

		return 0;
	}
	public Integer countAllPagingIncludeDuplicateTeams(String name, String dob, String email, String phone,
			List<Integer> teamIds, List<Integer> positionIds, String sort, String order, Integer offset,
			Integer limit) {
		Set<Employee> employeeSet = new LinkedHashSet<>();
		StringBuilder hql = new StringBuilder();
		hql.append("from ");
		hql.append("employees emp ");
		hql.append("inner join emp.positions as pos inner join emp.departments as dep ");
		hql.append("where pos.team.id is not null ");
		hql.append("and pos.department.id is null ");
		hql.append("and emp.activeFlag = true ");
		if(!name.equals("")) {
			hql.append("and emp.name like CONCAT('%',:name,'%') ");
		}
		if(!dob.equals("")) {
			hql.append("and emp.dateOfBirth like CONCAT('%',:dob,'%') ");
		}
		if(!email.equals("")) {
			hql.append("and emp.email like CONCAT('%',:email,'%') ");
		}
		if(!phone.equals("")) {
			hql.append("and emp.phoneNumber like CONCAT('%',:phone,'%') ");
		}
		if(teamIds.size()>0) {
			hql.append("and pos.team.id IN (:teamIds) ");
		}
		if(positionIds.size()>0) {
			hql.append("and pos.id IN (:positionIds) ");

		}
		
		hql.append("order by " + sort + " " + order);
		Session session = this.sessionFactory.getCurrentSession();
		List<EmployeeModel> employeeModelList = new ArrayList();
		try {
			Query query = session.createQuery(hql.toString());
			if(!name.equals("")) {
				query.setParameter("name", name);
			}
			if(!dob.equals("")) {
				query.setParameter("dob", dob);
			}
			if(!email.equals("")) {
				query.setParameter("email", email);
			}
			if(!phone.equals("")) {
				query.setParameter("phone", phone);
			}
			if(teamIds.size()>0) {
				query.setParameter("teamIds", teamIds);
			}
			if(positionIds.size()>0) {
				query.setParameter("positionIds", positionIds);

			}
			if (offset > 0) {
				query.setFirstResult(offset);

			}
			if (limit > 0) {
				query.setMaxResults(limit);
			}
//			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
//				Object[] ob = (Object[]) it.next();
//				employeeSet.add((Employee) ob[0]);
//			}
			Integer countResult = query.getResultList().size();
			return countResult;
		} catch (Exception e) {
			LOGGER.error("Error has occured in employeePaging() ", e);

		}

		return 0;
	}

// Paging with team - end
	
	@Override
	public Employee findById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		hql.append("from employees emp ");
//		hql.append("INNER JOIN emp.positions as pos ");
		hql.append("where emp.id = :id");
		Employee employee = null;
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
//			Iterator it = query.getResultList().iterator();
//			Object ob = (Object) it.next();
			employee = (Employee) query.getSingleResult();
			
			// Please don't delete this line, this fix lazy load error when load position
			employee.getPositions().size();
			employee.getDepartments().size();
			employee.getTeams().size();
			// Please don't delete this line, this fix lazy load error when load position
		} catch (Exception e) {
			LOGGER.error("Error has occured in findById() ", e);
		}
		return employee;
	}

	@Override
	public boolean add(Set<Employee> emps) {
		for (Employee em : emps) {
			if (add(em) == -1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Set<EmployeeModel> findByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		Set<EmployeeModel> empModelSet = new LinkedHashSet<>();
		hql.append("from employees emp ");
		hql.append("where emp.name like CONCAT('%',:name,'%') ");
		hql.append("order by emp.name asc");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("name", name);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Employee employee = (Employee) it.next();
				EmployeeModel employeeModel = new EmployeeModel();
				employeeModel.setId(employee.getId());
				employeeModel.setCode(employee.getCode());
				employeeModel.setName(employee.getName());
				employeeModel.setAvatar(employee.getAvatar());
				empModelSet.add(employeeModel);
			}
			return empModelSet;
		} catch (Exception e) {
			LOGGER.error("Error has occured in findByName() ", e);
			return null;
		}
	}
	public List<Employee> findByPositionId(Integer positionId){
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		List<Employee> employees = new ArrayList();
		hql.append("from employees emp ");
		hql.append("inner join emp.positions as pos ");
		hql.append("where pos.id = :positionId ");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("positionId", positionId);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] ob =  (Object[]) it.next();
				Employee employee = (Employee) ob[0];
				employees.add(employee);
			}
			return employees;
		} catch (Exception e) {
			LOGGER.error("Error has occured in findByPositionId() ", e);
			return null;
		}
	}
	
	public List<Employee> findByDepartmentId(Integer departmentId){
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hql = new StringBuilder();
		List<Employee> employees = new ArrayList();
		hql.append("from employees emp ");
		hql.append("inner join emp.departments as dep ");
		hql.append("where dep.id = :departmentId ");
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("departmentId", departmentId);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] ob =  (Object[]) it.next();
				Employee employee = (Employee) ob[0];
				employees.add(employee);
			}
			return employees;
		} catch (Exception e) {
			LOGGER.error("Error has occured in findByDepartmentId() ", e);
			return null;
		}
	}
	public EmployeeModel toModel(Employee e) {
		EmployeeModel employeeModel = new EmployeeModel();
		List<PositionModel> positionModelList = new ArrayList<>();
		List<DepartmentModel> departmentModelList = new ArrayList<>();
		List<TeamModel> teamModelList = new ArrayList<>();
		// Add team list
		// Add position list
		// Add department list
		for (Position p : e.getPositions()) {
			PositionModel positionModel = new PositionModel();
			Role role = new Role();
			role.setId(p.getRole().getId());
			role.setName(p.getRole().getName());
			positionModel.setId(p.getId());
			positionModel.setName(p.getName());
			positionModel.setIsManager(p.getIsManager());
			positionModel.setRole(role);
			if (p.getDepartment() != null && p.getTeam() == null) {
				Department department = p.getDepartment();
				DepartmentModel departmentModel = new DepartmentModel();
				departmentModel.setId(department.getId());
				departmentModel.setCode(department.getCode());
				departmentModel.setName(department.getName());
				departmentModel.setFatherDepartmentId(department.getFatherDepartmentId());
				departmentModel.setHeadPosition(department.getHeadPosition());
				departmentModel.setDescription(department.getDescription());
				departmentModel.setLevel(department.getLevel());
				departmentModel.setPosition(positionModel);
				departmentModelList.add(departmentModel);
			} else if (p.getDepartment() == null && p.getTeam() != null) {
				Team team = p.getTeam();
				TeamModel teamModel = new TeamModel();
				teamModel.setId(team.getId());
				teamModel.setCode(team.getCode());
				teamModel.setName(team.getName());
				teamModel.setDescription(team.getDescription());
				teamModel.setHeadPosition(team.getHeadPosition());
				teamModel.setPosition(positionModel);
				teamModelList.add(teamModel);
			}
		}
		UserModel user = new UserModel();
		user.setUsername(e.getUsername());
		user.setEnableLogin(e.isEnableLogin());
		employeeModel.setId(e.getId());
		employeeModel.setCode(e.getCode());
		employeeModel.setName(e.getName());
		employeeModel.setAvatar(e.getAvatar());
		employeeModel.setGender(e.getGender());
		employeeModel.setDateOfBirth(e.getDateOfBirth());
		employeeModel.setEmail(e.getEmail());
		employeeModel.setPhoneNumber(e.getPhoneNumber());
		employeeModel.setActive(e.isActive());
		employeeModel.setCreateDate(e.getCreateDate());
		employeeModel.setDepartments(departmentModelList);
		;
		employeeModel.setPositions(positionModelList);
		employeeModel.setUser(user);
		employeeModel.setCreateDate(e.getCreateDate());
		employeeModel.setModifyDate(e.getModifyDate());
		employeeModel.setCreateBy(e.getCreateBy());
		employeeModel.setModifyBy(e.getModifyBy());
		employeeModel.setTeams(teamModelList);
		return employeeModel;
	}
}
