package com.comaymanagement.cmd.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.comaymanagement.cmd.model.Response;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

@Data
@Entity(name = "mail_schedule")
@ToString
@JsonInclude(Include.NON_NULL)
public class MailSchedule implements Serializable {

    private static final long serialVersionUID = 6321323265487281667L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @Column(name = "username")
    private String username;

    @Column(name = "to_email")
    private String toEmail;

    @Column(name = "schedule_datetime")
    private String scheduleDateTime;

    @Column(name = "schedule_zoneId")
    private String scheduleZoneId;

    @Column(name = "is_deleted")
    private boolean isDeleted;
    public Response toResponse() {
        Response response = new Response();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        response.setScheduleId(this.scheduleId);
        response.setUsername(this.username);
        response.setToEmail(this.toEmail);
        response.setScheduledTime(LocalDateTime.parse(this.scheduleDateTime, formatter));
        response.setZoneId(this.scheduleZoneId);
        return response;
    }
}
