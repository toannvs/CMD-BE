package com.comaymanagement.cmd.repository;

import org.springframework.stereotype.Repository;

import com.comaymanagement.cmd.model.UserModel;


@Repository
public interface UserRepository{
    UserModel findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
