package com.comaymanagement.cmd.constant;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CMDConstrant {
	public static final String PASSWORD = "cmdcmdcmd";
	public static final Integer LIMIT = 15;
	public static final Path path = Paths.get("D:\\CMD-proect\\Source\\CMD");
	public static final Integer ROLELIMIT = 10;
	public static final Integer FAILED = -1;
	public static final Integer DONE_STATUS = 4;
	public static final Integer CANCEL_STATUS = 5;
	public static final Integer NEW_STATUS = 1;
	public static final Integer INPROGESS_STATUS = 2;
	public static final Integer REVIEW_STATUS = 3;
	public static final Integer PROPOSAL_PENDING_STATUS_INDEX = 1;
	public static final Integer PROPOSAL_COMPLETE_STATUS_INDEX = 2;
	public static final Integer PROPOSAL_DENIED_STATUS_INDEX = 3;
	public static final Integer PROPOSAL_CANCELLED_STATUS_INDEX = 4;
	public static final boolean UPDATE_TASK = true;
	public static final boolean CHANGE_STATUS_TASK = true;
	public static final boolean REOPEN_TASK = true;
	public static final String SPACE = " ";
}

