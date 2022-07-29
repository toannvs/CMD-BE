package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.Post;
import com.comaymanagement.cmd.model.LikeModel;

public interface IPostRepository {
	public List<Post> findAll(String title, String content, String sort, String order);
	public Integer add(Post post);
	public Integer edit(Post post);
	public Post findById(Integer id);
	public String delete(Integer id);
	public LikeModel like(Integer postId);
}
