package com.comaymanagement.cmd.jobs;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.comaymanagement.cmd.repository.MailScheduleDao;
import com.comaymanagement.cmd.service.MailService;
import static com.comaymanagement.cmd.constant.ScheduleContrant.*;
public class MailScheduleJob extends QuartzJobBean {

    @Autowired
    private MailService mailService;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    MailScheduleDao mailScheduleDao;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        String subject = jobDataMap.getString(SUBJECT);
        String message = jobDataMap.getString(MESSAGE);
        String toMail = jobDataMap.getString(TO_MAIL);
        Integer scheduleId = jobDataMap.getInt(SCHEDULE_ID);

        mailService.sendMail(from, toMail, subject, message);
        mailScheduleDao.deleteMailSchedule(scheduleId);
    }
}
