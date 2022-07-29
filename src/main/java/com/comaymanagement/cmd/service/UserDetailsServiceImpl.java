package com.comaymanagement.cmd.service;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.model.UserModel;
import com.comaymanagement.cmd.repository.UserRepository;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    /**
     * Override phương thức trong class UserDetailsService
     *
     * @param username
     * @return UserDetailsImpl là implements của UserDetails (UserDetails là đối tượng Spring security sử dụng để authen và authorize)
     * @throws UsernameNotFoundException
     */
    @Override
    @Transactional
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
    	UserModel user = null;
		user = userRepository.findByUsername(username);
		if(user == null) {
			throw new UsernameNotFoundException("User Not Found with username: " + username);
		}
        return UserDetailsImpl.build(user);
    }
}