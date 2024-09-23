package com.fiuni.mytube_users.dto;

import com.fiuni.mytube.dto.base.BaseDTO;
import lombok.*;


@Data
@AllArgsConstructor
public class UserDTOCreate extends BaseDTO {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String email;


}
