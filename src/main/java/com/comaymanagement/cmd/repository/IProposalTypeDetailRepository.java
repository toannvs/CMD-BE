package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.ProposalTypeDetail;

public interface IProposalTypeDetailRepository {
	public List<ProposalTypeDetail> findById(Integer id);
}
