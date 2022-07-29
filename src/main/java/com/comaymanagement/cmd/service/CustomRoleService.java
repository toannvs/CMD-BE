package com.comaymanagement.cmd.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.comaymanagement.cmd.model.OptionModel;
import com.comaymanagement.cmd.model.PermissionModel;
import com.comaymanagement.cmd.model.RoleDetailModel;
/**
All option name
	todolist
	request
	type
	employee
	department
	position
	inventory
	team
All permission name
 	view
 	create
 	update
 	detele
 	view_all
 	update_all
 	delete_all
 **/
@Service("customRoleService")
public class CustomRoleService {
	public static final String VIEW = "view";
	public static final String CREATE = "create";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";
	public static final String VIEW_ALL = "view_all";
	public static final String UPDATE_ALL = "update_all";
	public static final String DELETE_ALL = "delete_all";
	public static final String IMPORT = "import";
	public static String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7, token.length());
        }

        return null;
    }
	public boolean canView(String option, UserDetailsImpl userDetail) {
		return authorization(option, userDetail, VIEW);
	}
	
	public boolean canCreate(String option, UserDetailsImpl userDetail) {
		return authorization(option, userDetail, CREATE);
	}
	
	public boolean canUpdate(String option, UserDetailsImpl userDetail) {
		return authorization(option, userDetail, UPDATE);
	}
	
	public boolean canDelete(String option, UserDetailsImpl userDetail) {
		return authorization(option, userDetail, DELETE);
	}
	
	public boolean canViewAll(String option, UserDetailsImpl userDetail) {
		return authorization(option, userDetail, VIEW_ALL);
	}
	
	public boolean canUpdateAll(String option, UserDetailsImpl userDetail) {
		return authorization(option, userDetail, UPDATE_ALL);
	}
	
	public boolean canDeleteAll(String option, UserDetailsImpl userDetail) {
		return authorization(option, userDetail, DELETE_ALL);
	}

	public boolean canImport(String option, UserDetailsImpl userDetail) {
		return authorization(option, userDetail, IMPORT);
	}
	
	public boolean isTheSameUser(Integer empId, UserDetailsImpl userDetail) {
			if(userDetail.getId().equals(empId)) {
				return true;
			}
			return false;
	}
	
	public boolean authorization(String option, UserDetailsImpl userDetail, String action) {
		// loop through roles
		for(RoleDetailModel r : userDetail.getRoles()) {
			// loop through options in role
			for(OptionModel optionModel : r.getOptions()) {
				// If same with option => // loop through permission in option
				if(optionModel.getName().equals(option)) {
					for(PermissionModel p : optionModel.getPermissions()) {
						if (p.getName().equals(action) && p.isSelected() == true) {
							return true;
						}
					}
				}
			}
			
		}
		return false;
	}
	
}
