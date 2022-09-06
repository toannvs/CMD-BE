package com.comaymanagement.cmd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comaymanagement.cmd.service.PostService;

@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PostController {
	@Autowired
	PostService postService;
	@PreAuthorize("@customRoleService.canView('post',principal)")
	@GetMapping("")
	public ResponseEntity<Object> findAll(
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "content", required = false) String content
			) {
		// sort and order with default
		return postService.findAll(title, content, null, null);
	}
	
	@PreAuthorize("@customRoleService.canCreate('post',principal)")
	@PostMapping("/add")
	public ResponseEntity<Object> add(@RequestBody String json) {
		return postService.add(json);
	}
	
	@PreAuthorize("@customRoleService.canUpdate('post',principal)")
	@PutMapping("/edit")
	public ResponseEntity<Object> edit(
			@RequestBody String json) {
		return postService.edit(json);
	}
	
	@PreAuthorize("@customRoleService.canDelete('post',principal)")
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Object> delete(
			@PathVariable Integer id) {
		return postService.delete(id);
	}
	
//	@PreAuthorize("@customRoleService.canDelete('post',principal)")
	@GetMapping("/{postId}/likes")
	public ResponseEntity<Object> like(
			@PathVariable(name = "postId") Integer postId) {
		return postService.like(postId);
	}
	
}
