package com.comaymanagement.cmd.repositoryimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.comaymanagement.cmd.entity.DataType;
import com.comaymanagement.cmd.repository.IDataTypeRepository;

@Repository
@Transactional(rollbackFor = Exception.class)
public class DataTypeRepository implements IDataTypeRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataTypeRepository.class);
	@Autowired
	private SessionFactory sessionFactory;
	public List<DataType> findAll(){
		List<DataType> dataTypes = new ArrayList<>();
		Session session = sessionFactory.getCurrentSession();
		String hql = "from data_types";
		try {
			Query query = session.createQuery(hql.toString());
			LOGGER.info(hql.toString());
			for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
				DataType dataType = (DataType) it.next();
				dataTypes.add(dataType);
			}
			return dataTypes;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}
}
