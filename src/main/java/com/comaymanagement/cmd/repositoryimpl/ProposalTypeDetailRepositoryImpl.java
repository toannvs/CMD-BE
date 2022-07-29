package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.ProposalTypeDetail;
import com.comaymanagement.cmd.model.ProposalTypeDetailModel;
import com.comaymanagement.cmd.model.ProposalTypeModel;
import com.comaymanagement.cmd.repository.IProposalTypeDetailRepository;
@Repository
@Transactional(rollbackFor = Exception.class)
public class ProposalTypeDetailRepositoryImpl implements IProposalTypeDetailRepository {
private static final Logger LOGGER = LoggerFactory.getLogger(PositionRepositoryImpl.class);
	
	@Autowired
	SessionFactory sessionFactory;
	@Override
	public List<ProposalTypeDetail> findById(Integer id) {
		StringBuilder hql = new StringBuilder();
		List<ProposalTypeDetail> proposalTypeDetails = new ArrayList<>();
		
		hql.append("FROM proposal_type_details pro_type ");
		hql.append("INNER JOIN data_types as dt on dt.id = pro_type.dataType.id ");
		hql.append("WHERE pro_type.proposalType.id = :id ");
		hql.append("ORDER BY pro_type.fieldId asc");
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			query.setParameter("id", id);
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				Object[] obj = (Object[]) it.next();
				ProposalTypeDetail proposalTypeDetail = (ProposalTypeDetail) obj[0];
//				DataType dataType = (DataType) obj[1];
				proposalTypeDetails.add(proposalTypeDetail);
			}
			return proposalTypeDetails;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
	public List<ProposalTypeDetailModel> toModel(List<ProposalTypeDetail> proposalTypeDetails) {
		List<ProposalTypeDetailModel> proTypeDetailModels = new ArrayList<>();
		for(ProposalTypeDetail proTypeDetail : proposalTypeDetails) {
			ProposalTypeDetailModel proTypeDetailModel = new ProposalTypeDetailModel();
			proTypeDetailModel.setId(proTypeDetail.getId());
			proTypeDetailModel.setName(proTypeDetail.getName());
			proTypeDetailModel.setLabel(proTypeDetail.getLabel());
			proTypeDetailModel.setPlaceholder(proTypeDetail.getPlaceholder());
			proTypeDetailModel.setRequired(proTypeDetail.isRequired());
			proTypeDetailModel.setDescription(proTypeDetail.getDescription());
			proTypeDetailModel.setFeedback(proTypeDetail.getFeedback());
			proTypeDetailModel.setDataType(proTypeDetail.getDataType());
			proTypeDetailModels.add(proTypeDetailModel);
		}
		return proTypeDetailModels;
	}
}
