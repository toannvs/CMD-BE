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

import com.comaymanagement.cmd.service.ApprovalStepService;

@RestController
@RequestMapping("/proposaal-type-step-config")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApprovalStepController {
	@Autowired
	ApprovalStepService approvalStepService;
	
	
	@PreAuthorize("@customRoleService.canView('proposal',principal)")
	@GetMapping("")
	public ResponseEntity<Object> findAllByProposalId(@RequestParam(value = "proposalTypeId", required = true) Integer proposalTypeId){
		return approvalStepService.findAllByProposalId(proposalTypeId);
	}
	
	@PreAuthorize("@customRoleService.canCreate('proposal',principal)")
	@PostMapping("/add")
	public ResponseEntity<Object> add(@RequestBody String json){
		return approvalStepService.add(json);
	}
	@PreAuthorize("@customRoleService.canDelete('proposal',principal)")
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Object> delete(@PathVariable Integer id){
		return approvalStepService.delete(id);
	}
	@PreAuthorize("@customRoleService.canUpdate('proposal',principal)")
	@PutMapping("/edit")
	public ResponseEntity<Object> edit(@RequestBody String json){
		return approvalStepService.edit(json);
	}
}
