package com.comaymanagement.cmd.repository;

import java.util.List;

import com.comaymanagement.cmd.entity.Proposal;
import com.comaymanagement.cmd.entity.ProposalDetail;
import com.comaymanagement.cmd.model.ProposalModel;


public interface IProposalRepository {
	public List<ProposalModel> findAllProposalApproveByMe(Integer employeeId,Integer proposalTypeId, List<Integer> statusId, Integer creator,
			String createDate, String finishDate, String sort, String order, Integer offset, Integer limit);
	public Integer countAllPaging(Integer employeeId,Integer proposal, String content, String status, String creator,
			String createDateFrom, String createDateTo, String sort, String order, Integer offset, Integer limit);
	public ProposalModel findById(Integer id);
	public ProposalModel add(Proposal proposal, List<ProposalDetail> proposalDetails);
}
