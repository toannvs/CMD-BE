package com.comaymanagement.cmd.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.entity.MailSchedule;

import java.util.List;
import java.util.Optional;

//@Repository
public interface MailScheduleRepository{

    public MailSchedule findByScheduleIdAndIsDeletedFalse(Integer scheduleId);

    public List<MailSchedule> findByUsernameAndIsDeletedFalse(String username);

    public boolean existsByScheduleIdAndIsDeletedFalse(Integer scheduleId);
    
    public MailSchedule save(MailSchedule mailSchedule);
    public MailSchedule findById(Integer scheduleId);
}
