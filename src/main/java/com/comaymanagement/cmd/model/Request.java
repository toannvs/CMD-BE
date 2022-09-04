package com.comaymanagement.cmd.model;

import com.comaymanagement.cmd.entity.RoleDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@ToString
public class Request {

    private Integer scheduleId;

    @NotNull(message = "username cannot be null")
    private String username;

    @Email(message = "Provided email format is not valid")
    @NotNull(message = "toEmail cannot be null")
    private String toEmail;

    @NotNull(message = "subject cannot be null")
    private String subject;

    @NotNull(message = "message cannot be null")
    private String message;

    @NotNull(message = "scheduledTime cannot be null")
    private String scheduledTime;

    @NotNull(message = "zoneId cannot be null")
    private String zoneId;

}
