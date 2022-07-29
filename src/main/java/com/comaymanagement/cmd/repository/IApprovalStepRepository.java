package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.ApprovalStep;

public interface IApprovalStepRepository {
	public List<ApprovalStep> findByProposalTypeId(Integer proposalTypeId);
}
