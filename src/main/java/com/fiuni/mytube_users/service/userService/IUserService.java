package com.fiuni.mytube_users.service.userService;

import com.fiuni.mytube.domain.user.UserDomain;
import com.fiuni.mytube.dto.user.*;
import com.fiuni.mytube_users.dto.UserDTOComplete;
import com.fiuni.mytube_users.dto.UserDTOCreate;
import com.fiuni.mytube_users.service.baseService.IBaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService extends IBaseService<UserDTO, UserResult> {
    UserDTO createUser(UserDTOCreate dto);
    void changePassword(Integer id, UserDTOCreate dto);
    void deleteUser(Integer id);
    UserResult getAll(Pageable pageable);
    UserDTOComplete updateUser(Integer id, UserDTOComplete dto);
}
