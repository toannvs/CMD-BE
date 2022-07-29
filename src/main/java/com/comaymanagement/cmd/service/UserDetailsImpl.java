package com.comaymanagement.cmd.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.comaymanagement.cmd.model.OptionModel;
import com.comaymanagement.cmd.model.RoleDetailModel;
import com.comaymanagement.cmd.model.UserModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private Integer id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private List<RoleDetailModel> roles;
    private Collection<? extends GrantedAuthority> authorities;
    
    public static UserDetailsImpl build(UserModel user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        List<RoleDetailModel> roleDetailModels = null;
        try {
        	roleDetailModels = user.getRoles();
        	 for(RoleDetailModel roleDetail : roleDetailModels) {
             	for(OptionModel op : roleDetail.getOptions()) {
             		 authorities.add(new SimpleGrantedAuthority(op.getName()));
             	}
             }
		} catch (Exception e) {
			LOGGER.error("Have error at build(): ", e);
		}
       
       
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                roleDetailModels,
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    

	@Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}