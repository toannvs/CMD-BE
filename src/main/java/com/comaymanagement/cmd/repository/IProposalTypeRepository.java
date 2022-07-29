package com.comaymanagement.cmd.repository;

import com.comaymanagement.cmd.entity.ProposalType;

public interface IProposalTypeRepository {
	ProposalType findById(String id);
}
