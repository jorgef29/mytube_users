package com.fiuni.mytube_users.dto;

import com.fiuni.mytube.dto.base.BaseDTO;
import lombok.*;


@Data
@AllArgsConstructor
public class UserDTOCreate extends BaseDTO {
    private String username;
    private String password;
    private String email;


}
