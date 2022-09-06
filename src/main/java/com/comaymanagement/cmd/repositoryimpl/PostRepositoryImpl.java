package com.comaymanagement.cmd.repositoryimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Post;
import com.comaymanagement.cmd.model.LikeModel;
import com.comaymanagement.cmd.model.PostModel;
import com.comaymanagement.cmd.repository.IPostRepository;
import com.comaymanagement.cmd.service.UserDetailsImpl;
@Repository
@Transactional(rollbackFor = Exception.class)
public class PostRepositoryImpl implements IPostRepository{
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	EmployeeRepositoryImpl employeeRepository;
	
	@Override
	public List<Post> findAll(String title, String content, String sort, String order) {
		StringBuilder hql = new StringBuilder();
		List<Post> posts = new ArrayList<>();
		hql.append("from posts post ");
		hql.append("where post.title like CONCAT('%',:title,'%') ");
		hql.append("and post.content like CONCAT('%',:content,'%') ");
		hql.append("order by " + sort + " " + order);
		Session session = this.sessionFactory.getCurrentSession();
		try {
			Query query = session.createQuery(hql.toString());
			query.setParameter("title", title);
			query.setParameter("content", content);
			LOGGER.info(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Post post = (Post) it.next();
				posts.add(post);
			}
			return posts;
		} catch (Exception e) {
			LOGGER.error("Error has occured in findAll() ", e);
			return null;
		}
	}
	@Override
	public Post findById(Integer id) {
		Post post = new Post();
		StringBuilder hql = new StringBuilder();
		hql.append("FROM posts post ");
		hql.append("WHERE post.id = :id");
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			query.setParameter("id", id);
			post  = (Post) query.getSingleResult();
			post.getEmployees().size();
			return post;
		} catch (Exception e) {
			LOGGER.error("Error has occured in findById() ", e);
			return null;
		}
		
		
	}
	@Override
	public Integer add(Post post) {
		Session session = sessionFactory.getCurrentSession();
		try {
			Integer id = (Integer) session.save(post);
			return id;
		} catch (Exception e) {
			LOGGER.error("Error has occured at add() ", e);
		}
		return -1;
	}

	@Override
	public Integer edit(Post post) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(post);
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
			Post post = session.find(Post.class, id);
			session.remove(post);
			return "1";
		} catch (Exception e) {
			LOGGER.error("Error has occured in delete() ", e);
			return "0";
		}
	}
	@Override
	public PostModel like(Integer postId) {
		List<Employee> employees = null;
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Employee employee = employeeRepository.findById(userDetail.getId());
		Post post = findById(postId);
		if(null != post && null != post.getEmployees()) {
			employees = post.getEmployees();
			boolean like = true;
			for(Employee item: employees) {
				if(item.getId()==employee.getId()) {
					like = false;
					break;
				}
			}
			if(like) {
					employees.add(employee);
					post.setLikeTotal(post.getLikeTotal()+1);
				
			}else {
				if(post.getLikeTotal()!=0) {
					employees.remove(employee);
					post.setLikeTotal(post.getLikeTotal()-1);
				}else {
					post.setLikeTotal(0);
				}
				
			}
		}else {
			return null;
		}
		Integer result = edit(post);
		if(result != 1) {
			return null;
		}
		// Response data
		PostModel postModel = toModel(post);
		postModel.setLike(checkIsLike(post.getId()));
//		LikeModel likeModel = new LikeModel();
//		likeModel.setPostId(postId);
//		likeModel.setLikeTotal(post.getLikeTotal());
//		String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
//		likeModel.setCreateDate(createDate);
		return postModel;
	}
	
	public PostModel toModel(Post post) {
		PostModel postModel = new PostModel();
		postModel.setId(post.getId());
		postModel.setTitle(post.getTitle());
		postModel.setContent(post.getContent());
		postModel.setPulished(post.isPulished());
		postModel.setCreator(post.getCreator());
		postModel.setEditor(post.getEditor());
		postModel.setCreateDate(post.getCreateDate());
		postModel.setModifyDate(post.getModifyDate());
		postModel.setLikeTotal(post.getLikeTotal());
		return postModel;
	}
	public boolean checkIsLike(Integer postId) {
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		List<Employee> employees = new ArrayList<>();
		Post post = findById(postId);
		boolean isLike = false;
		if(null != post && null != post.getEmployees()) {
			employees = post.getEmployees();
			for(Employee item: employees) {
				if(item.getId()==userDetail.getId()) {
					isLike = true;
					break;
				}
			}
		}
		return isLike;
	}
}
