package com.comaymanagement.cmd.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.exception.BadRequestException;
import com.comaymanagement.cmd.model.Request;
import com.comaymanagement.cmd.model.Response;
import com.comaymanagement.cmd.repository.MailScheduleDao;

@Service
public class MailScheduleService{

    @Autowired
    private MailScheduleDao mailScheduleDao;

    public String createSchedule(Request request) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	Date start = new Date();
		try {
			start = format.parse(request.getScheduledTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        ZonedDateTime zonedDateTime = ZonedDateTime.of(request.getScheduledTime(), request.getZoneId());
//        if (start.before(Date.now())) {
//            throw new BadRequestException("Scheduled Time should be greater than current time");
//        }
        return mailScheduleDao.createSchedule(request, start);
    }

    public Response getSchedule(int id, String username) {
        Response response = mailScheduleDao.getSchedule(id, username);
        if (response == null) {
            throw new BadRequestException(String.format("No Active Schedule exists with id : %s", id));
        }
        return response;
    }

    public List<Response> getSchedules(String username, int page, int size) {
        return mailScheduleDao.getSchedules(username, page, size);
    }

    public String updateSchedule(Request request) {
        checkIfScheduleExists(request.getScheduleId(), request.getUsername());
//        ZonedDateTime zonedDateTime = ZonedDateTime.of(request.getScheduledTime(), request.getZoneId());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	Date start = new Date();
		try {
			start = format.parse(request.getScheduledTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return mailScheduleDao.updateSchedule(request, start);
    }

    public String deleteSchedule(int id) {
        mailScheduleDao.deleteSchedule(id);
        return String.valueOf(id);
    }

    private void checkIfScheduleExists(int id, String username) {
        boolean exists = mailScheduleDao.checkIfScheduleExists(username, id);
        if (!exists) {
            throw new BadRequestException(String.format("An active schedule with id %s does not exist", id));
        }
    }
}
