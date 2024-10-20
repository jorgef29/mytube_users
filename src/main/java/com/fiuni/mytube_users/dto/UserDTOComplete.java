package com.fiuni.mytube_users.dto;


import com.fiuni.mytube.dto.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTOComplete extends BaseDTO {
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private Date birthday;
}
