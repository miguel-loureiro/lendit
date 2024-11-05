package com.ims.models.dtos.response;

import com.ims.models.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateResponseDto {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private String profileImage;
}