package com.comaymanagement.cmd.repositoryimpl;


import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.transaction.Transactional;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.entity.MailSchedule;
import com.comaymanagement.cmd.exception.InternalServerException;
import com.comaymanagement.cmd.jobs.MailScheduleJob;
import com.comaymanagement.cmd.model.Request;
import com.comaymanagement.cmd.model.Response;
import com.comaymanagement.cmd.repository.MailScheduleDao;
import com.comaymanagement.cmd.repository.MailScheduleRepository;
import com.comaymanagement.cmd.utils.AppUtils;

import static com.comaymanagement.cmd.constant.ScheduleContrant.*;
@Transactional
@Repository
public class MailScheduleDaoImpl implements MailScheduleDao {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private MailScheduleRepository mailScheduleRepository;

    @Override
    @Transactional
    public String createSchedule(Request request, Date zonedDateTime) {
        String scheduleId = saveSchedule(request);
        JobDetail jobDetail = getJobDetail(scheduleId, request);
        Trigger simpleTrigger = getSimpleTrigger(jobDetail, zonedDateTime);
        scheduleJob(jobDetail, simpleTrigger);
        return scheduleId;
    }

    public String saveSchedule(Request request) {
        try {
            MailSchedule save = mailScheduleRepository.save(toMailSchedule(request));
            return save.getScheduleId().toString();
        } catch (Exception e) {
            throw new InternalServerException("Unable to save schedule to DB");
        }
    }
    public MailSchedule toMailSchedule(Request request) {
        MailSchedule mailSchedule = new MailSchedule();
        mailSchedule.setDeleted(false);
        mailSchedule.setScheduleId(request.getScheduleId());
        mailSchedule.setUsername(request.getUsername());
        mailSchedule.setToEmail(request.getToEmail());
        mailSchedule.setScheduleDateTime(request.getScheduledTime().toString());
        mailSchedule.setScheduleZoneId(request.getZoneId().toString());
        return mailSchedule;
    }
    public JobDetail getJobDetail(String scheduleId, Request request) {
        Integer jobId = Integer.valueOf(scheduleId);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(TO_MAIL, request.getToEmail());
        jobDataMap.put(SUBJECT, request.getSubject());
        jobDataMap.put(MESSAGE, request.getMessage());
        jobDataMap.put(SCHEDULE_ID, scheduleId);

        return JobBuilder.newJob(MailScheduleJob.class)
                .withIdentity(String.valueOf(jobId), JOB_DETAIL_GROUP_ID)
                .withDescription(JOB_DETAIL_DESCRIPTION)
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    public Trigger getSimpleTrigger(JobDetail jobDetail, Date zonedDateTime) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), TRIGGER_GROUP_ID)
                .withDescription(TRIGGER_DESCRIPTION)
                .startAt(zonedDateTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    public Trigger getCronTrigger(JobDetail jobDetail, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), TRIGGER_GROUP_ID)
                .withDescription(TRIGGER_DESCRIPTION)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).
                        withMisfireHandlingInstructionFireAndProceed().inTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC)))
                .build();
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException schedulerException) {
            throw new InternalServerException("Error creating the schedule");
        }
    }

    @Override
    public Response getSchedule(int scheduleId, String username) {
        Response response = null;
        try {
           MailSchedule mailSchedule = mailScheduleRepository.findByScheduleIdAndIsDeletedFalse(scheduleId);
            if (mailSchedule!=null) {
                response = mailSchedule.toResponse();
            }
        } catch (Exception e) {
            throw new InternalServerException("Error fetching the schedule");
        }
        return response;
    }

    @Override
    public List<Response> getSchedules(String username, int page, int size) {
        List<Response> responses = new ArrayList<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<MailSchedule> mailSchedules = mailScheduleRepository.findByUsernameAndIsDeletedFalse(username);
//            List<MailSchedule> mailSchedules = schedules.getContent();
            responses = AppUtils.getResponseDtoListFrom(mailSchedules);
        } catch (Exception e) {
            throw new InternalServerException("Unable to fetch all schedules from DB");
        }
        return responses;
    }

    @Override
    @Transactional
    public void deleteSchedule(int scheduleId) {
        deleteMailSchedule(scheduleId);
        deleteJobAndTrigger(scheduleId);
    }

    public void deleteMailSchedule(Integer scheduleId) {
        try {
            MailSchedule mailSchedule = mailScheduleRepository.findById(scheduleId);
            if (mailSchedule!=null) {
                mailSchedule.setDeleted(true);
                mailScheduleRepository.save(mailSchedule);
            }
        } catch (Exception e) {
            throw new InternalServerException("Error deleting schedule");
        }
    }

    public void deleteJobAndTrigger(Integer scheduleId) {
        try {
            scheduler.unscheduleJob(new TriggerKey(scheduleId.toString(), TRIGGER_GROUP_ID));
            scheduler.deleteJob(new JobKey(scheduleId.toString(), JOB_DETAIL_GROUP_ID));
        } catch (SchedulerException schedulerException) {
            throw new InternalServerException("Unable to delete the job from scheduler");
        }
    }

    @Override
    public boolean checkIfScheduleExists(String username, int id) {
        try {
            return mailScheduleRepository.existsByScheduleIdAndIsDeletedFalse(id);
        } catch (Exception e) {
            throw new InternalServerException("Error checking if schedule exists");
        }
    }

    @Override
    @Transactional
    public String updateSchedule(Request request, Date zonedDateTime) {
        updateMailSchedule(request);
        JobDetail jobDetail = updateJobDetail(request);
        updateTriggerDetails(request, jobDetail, zonedDateTime);
        return request.getScheduleId().toString();
    }

    public void updateMailSchedule(Request request) {
        try {
            MailSchedule mailSchedule = mailScheduleRepository.findByScheduleIdAndIsDeletedFalse(request.getScheduleId());
            if (mailSchedule!=null) {
                mailSchedule.setUsername(request.getUsername());
                mailSchedule.setToEmail(request.getToEmail());
                mailSchedule.setScheduleDateTime(request.getScheduledTime().toString());
                mailSchedule.setScheduleZoneId(request.getZoneId().toString());
                mailScheduleRepository.save(mailSchedule);
            }
        } catch (Exception e) {
            throw new InternalServerException("Unable to update the schedule in DB");
        }
    }

    public JobDetail updateJobDetail(Request request) {
        JobDetail jobDetail = null;
        try {
            if (request.getScheduleId() != null) {
                jobDetail = scheduler.getJobDetail(new JobKey(request.getScheduleId().toString(), JOB_DETAIL_GROUP_ID));
                jobDetail.getJobDataMap().put(TO_MAIL, request.getToEmail());
                jobDetail.getJobDataMap().put(SUBJECT, request.getSubject());
                jobDetail.getJobDataMap().put(MESSAGE, request.getMessage());
                scheduler.addJob(jobDetail, true);
            }
        } catch (SchedulerException schedulerException) {
            throw new InternalServerException("Unable to update the job data map");
        }
        return jobDetail;
    }

    public void updateTriggerDetails(Request request, JobDetail jobDetail, Date zonedDateTime) {
        Trigger newTrigger = getSimpleTrigger(jobDetail, zonedDateTime);
        try {
            scheduler.rescheduleJob(new TriggerKey(request.getScheduleId().toString(), TRIGGER_GROUP_ID), newTrigger);
        } catch (SchedulerException schedulerException) {
            throw new InternalServerException("Unable to update the trigger in DB");
        }
    }

}
