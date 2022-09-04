package com.comaymanagement.cmd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.model.Request;
import com.comaymanagement.cmd.model.Response;

@Repository
public interface MailScheduleDao {

    public String createSchedule(Request request, Date zonedDateTime);

    public Response getSchedule(int id, String username);

    public List<Response> getSchedules(String username, int page, int size);


    public String updateSchedule(Request request, Date zonedDateTime);

    public void deleteSchedule(int id);

    public boolean checkIfScheduleExists(String username, int id);

    public void deleteMailSchedule(Integer scheduleId);


}
