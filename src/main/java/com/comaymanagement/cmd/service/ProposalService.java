package com.comaymanagement.cmd.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.constant.CMDConstrant;
import com.comaymanagement.cmd.constant.Message;
import com.comaymanagement.cmd.entity.Employee;
import com.comaymanagement.cmd.entity.Pagination;
import com.comaymanagement.cmd.entity.Proposal;
import com.comaymanagement.cmd.entity.ProposalDetail;
import com.comaymanagement.cmd.entity.ProposalType;
import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.entity.Status;
import com.comaymanagement.cmd.model.ProposalModel;
import com.comaymanagement.cmd.repositoryimpl.EmployeeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.ProposalTypeRepositoryImpl;
import com.comaymanagement.cmd.repositoryimpl.StatusRepositotyImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
public class ProposalService {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	Message message;

	@Autowired
	ProposalRepositoryImpl proposalRepositoryImpl;


	@Autowired
	ProposalTypeRepositoryImpl proposalTypeRepositoryImpl;

	@Autowired
	EmployeeRepositoryImpl employeeRepositoryImpl;

	@Autowired
	StatusRepositotyImpl statusRepositotyImpl;

	public ResponseEntity<Object> findAllForAll(String json, String sort, String order, String page) {
		List<ProposalModel> proposalModels = new ArrayList<>();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String content = null;
		String createDateFrom = null;
		String createDateTo = null;
		List<Integer> creatorIds = new ArrayList<Integer>();
		List<Integer> proposalTypeIds = new ArrayList<>();
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		try {
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			JsonNode jsonCreatorIds = jsonObject.get("creatorIds");
			JsonNode jsonProposalTypeIds = jsonObject.get("proposalTypeIds");
			
			for (JsonNode creatorId : jsonCreatorIds) {
				creatorIds.add(Integer.valueOf(creatorId.toString()));
			}
			for(JsonNode proposalTypeId : jsonProposalTypeIds) {
				proposalTypeIds.add(Integer.valueOf(proposalTypeId.toString()));
			}
			createDateFrom = (jsonObject.get("createDateFrom") != null
					&& !jsonObject.get("createDateFrom").asText().equals("null")
					&& !jsonObject.get("createDateFrom").asText().equals(""))
							? jsonObject.get("createDateFrom").asText()
							: null;
			createDateTo = (jsonObject.get("createDateTo") != null
					&& !jsonObject.get("createDateTo").asText().equals("null")
					&& !jsonObject.get("createDateTo").asText().equals("")) ? jsonObject.get("createDateTo").asText()
							: null;
			page = page == null ? "1" : page.trim();
			int limit = CMDConstrant.LIMIT;
			int offset = (Integer.parseInt(page) - 1) * limit;
			List<Integer> statusIds = new ArrayList<Integer>();
			for (JsonNode statusId : jsonStatusObject) {
				System.out.println(statusId.toString());
				statusIds.add(Integer.valueOf(statusId.toString()));
			}
			if (statusIds.size() == 0) {
				List<Status> statuses = statusRepositotyImpl.findAll();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
			}
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "pro.createDate";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			proposalModels = proposalRepositoryImpl.findAllProposalForAll(proposalTypeIds,
					statusIds, creatorIds, createDateFrom, createDateTo, sort, order, offset, limit);

//			Integer totalProposal  = 0;
//			totalProposal = proposalRepositoryImpl.countAllPaging(userDetail.getId(), proposal, content, status, creator, createDate, finishDate, sort, order, offset, limit);
//			
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(proposalModels.size());

			Map<String, Object> results = new TreeMap<String, Object>();
			results.put("pagination", pagination);
			results.put("proposals", proposalModels);

			if (results.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> findAllApproveByMe(String json, String sort, String order, String page) {
		List<ProposalModel> proposalModels = new ArrayList<>();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String content = null;
		String createDateFrom = null;
		String createDateTo = null;
		List<Integer> creatorIds = new ArrayList<Integer>();
		List<Integer> proposalTypeIds = new ArrayList<>();
		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		try {
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			JsonNode jsonCreatorIds = jsonObject.get("creatorIds");
			JsonNode jsonProposalTypeIds = jsonObject.get("proposalTypeIds");
			
			for (JsonNode creatorId : jsonCreatorIds) {
				creatorIds.add(Integer.valueOf(creatorId.toString()));
			}
			for(JsonNode proposalTypeId : jsonProposalTypeIds) {
				proposalTypeIds.add(Integer.valueOf(proposalTypeId.toString()));
			}
			createDateFrom = (jsonObject.get("createDateFrom") != null
					&& !jsonObject.get("createDateFrom").asText().equals("null")
					&& !jsonObject.get("createDateFrom").asText().equals(""))
							? jsonObject.get("createDateFrom").asText()
							: null;
			createDateTo = (jsonObject.get("createDateTo") != null
					&& !jsonObject.get("createDateTo").asText().equals("null")
					&& !jsonObject.get("createDateTo").asText().equals("")) ? jsonObject.get("createDateTo").asText()
							: null;
			page = page == null ? "1" : page.trim();
			int limit = CMDConstrant.LIMIT;
			int offset = (Integer.parseInt(page) - 1) * limit;
			List<Integer> statusIds = new ArrayList<Integer>();
			for (JsonNode statusId : jsonStatusObject) {
				System.out.println(statusId.toString());
				statusIds.add(Integer.valueOf(statusId.toString()));
			}
			if (statusIds.size() == 0) {
				List<Status> statuses = statusRepositotyImpl.findAll();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
			}
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "pro.createDate";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			proposalModels = proposalRepositoryImpl.findAllProposalApproveByMe(userDetail.getId(), proposalTypeIds,
					statusIds, creatorIds, createDateFrom, createDateTo, sort, order, offset, limit);

//			Integer totalProposal  = 0;
//			totalProposal = proposalRepositoryImpl.countAllPaging(userDetail.getId(), proposal, content, status, creator, createDate, finishDate, sort, order, offset, limit);
//			
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(proposalModels.size());

			Map<String, Object> results = new TreeMap<String, Object>();
			results.put("pagination", pagination);
			results.put("proposals", proposalModels);

			if (results.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> findAllCreatedByMe(String json, String sort, String order, String page) {
		List<ProposalModel> proposalModels = new ArrayList<>();
		JsonMapper jsonMapper = new JsonMapper();
		JsonNode jsonObject;
		String content = null;
		Integer creator = null;
		String createDateFrom = null;
		String createDateTo = null;
		List<Integer> proposalTypeIds = new ArrayList<>();

		UserDetailsImpl userDetail = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		try {
			jsonObject = jsonMapper.readTree(json);
			JsonNode jsonStatusObject = jsonObject.get("statusIds");
			JsonNode jsonProposalTypeIds = jsonObject.get("proposalTypeIds");
			for(JsonNode proposalTypeId : jsonProposalTypeIds) {
				proposalTypeIds.add(Integer.valueOf(proposalTypeId.toString()));
			}
//			creator = (jsonObject.get("creator") != null && !jsonObject.get("creator").asText().equals("null")
//					&& jsonObject.get("creator").asInt() != 0) ? jsonObject.get("creator").asInt() : null;
			createDateFrom = (jsonObject.get("createDateFrom") != null
					&& !jsonObject.get("createDateFrom").asText().equals("null")
					&& !jsonObject.get("createDateFrom").asText().equals(""))
							? jsonObject.get("createDateFrom").asText()
							: null;
			createDateTo = (jsonObject.get("createDateTo") != null
					&& !jsonObject.get("createDateTo").asText().equals("null")
					&& !jsonObject.get("createDateTo").asText().equals("")) ? jsonObject.get("createDateTo").asText()
							: null;
			page = page == null ? "1" : page.trim();
			int limit = CMDConstrant.LIMIT;
			int offset = (Integer.parseInt(page) - 1) * limit;
			List<Integer> statusIds = new ArrayList<Integer>();
			for (JsonNode statusId : jsonStatusObject) {
				System.out.println(statusId.toString());
				statusIds.add(Integer.valueOf(statusId.toString()));
			}
			if (statusIds.size() == 0) {
				List<Status> statuses = statusRepositotyImpl.findAll();
				for (Status status : statuses) {
					statusIds.add(status.getId());
				}
			}
			// Order by defaut
			if (sort == null || sort == "") {
				sort = "pro.createDate";
			}
			if (order == null || order == "") {
				order = "desc";
			}
			// creator alway null
			proposalModels = proposalRepositoryImpl.findAllProposalCratedByMe(userDetail.getId(), proposalTypeIds,
					statusIds, createDateFrom, createDateTo, sort, order, offset, limit);

//			Integer totalProposal  = 0;
//			totalProposal = proposalRepositoryImpl.countAllPaging(userDetail.getId(), proposal, content, status, creator, createDate, finishDate, sort, order, offset, limit);
//			
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(Integer.valueOf(page));
			pagination.setTotalItem(proposalModels.size());

			Map<String, Object> results = new TreeMap<String, Object>();
			results.put("pagination", pagination);
			results.put("proposals", proposalModels);

			if (results.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", results));
			} else {
				pagination.setPage(1);
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", results));

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> findById(Integer id) {
		ProposalModel proposalModel = null;
		try {
			proposalModel = proposalRepositoryImpl.findById(id);
			if (null != proposalModel) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", proposalModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Not found", "Not found", ""));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}

	public ResponseEntity<Object> add(String json) {
		ProposalModel proposalModel = null;
		List<ProposalDetail> proposalDetails = null;
		try {
			JsonMapper jsonMapper = new JsonMapper();
			JsonNode jsonObjectProposal;
			JsonNode jsonObjectProposalDetails;
			jsonObjectProposal = jsonMapper.readTree(json);
			jsonObjectProposalDetails = jsonObjectProposal.get("proposalDetails");
			String proposalTypeId = jsonObjectProposal.get("proposalTypeId") != null
					? jsonObjectProposal.get("proposalTypeId").asText()
					: "-1";
			String creatorId = jsonObjectProposal.get("creatorId") != null
					? jsonObjectProposal.get("creatorId").asText()
					: "-1";
			String receiverId = jsonObjectProposal.get("receiverId") != null
					? jsonObjectProposal.get("receiverId").asText()
					: "-1";
			String statusId = jsonObjectProposal.get("statusId") != null ? jsonObjectProposal.get("statusId").asText()
					: "-1";

			String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
			Proposal proposal = new Proposal();
			proposal.setCreateDate(createDate);
			proposal.setModifyDate(createDate);
			proposal.setValidFlag(true);
			proposal.setCurrentStep("1");
			proposal.setModifyBy("-1");

			ProposalType proposalType = proposalTypeRepositoryImpl.findById(proposalTypeId);
			proposal.setProposalType(proposalType);

			Employee creator = employeeRepositoryImpl.findById(Integer.valueOf(creatorId));
			proposal.setCreator(creator);

			Employee receiver = employeeRepositoryImpl.findById(Integer.valueOf(receiverId));
			proposal.setReceiver(receiver);

			Status status = statusRepositotyImpl.findById(Integer.valueOf(statusId));
			proposal.setStatus(status);
			proposalDetails = new ArrayList<ProposalDetail>();
			for (JsonNode jsonObject : jsonObjectProposalDetails) {
				ProposalDetail proposalDetail = new ProposalDetail();
				String fieldId = jsonObject.get("fieldId") != null ? jsonObject.get("fieldId").asText() : "-1";
				String fieldName = jsonObject.get("fieldName") != null ? jsonObject.get("fieldName").asText() : "-1";
				String content = jsonObject.get("content") != null ? jsonObject.get("content").asText() : "-1";
				proposalDetail.setFieldId(fieldId);
				proposalDetail.setFieldName(fieldName);
				proposalDetail.setContent(content);
				proposalDetails.add(proposalDetail);
			}
			proposalModel = proposalRepositoryImpl.add(proposal, proposalDetails);

			if (null != proposalModel) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", "Query produce successfully: ", proposalModel));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("ERROR", message.getMessageByItemCode("DEPE3"), proposalModel));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseObject("ERROR", e.getMessage(), ""));
		}
	}
}
