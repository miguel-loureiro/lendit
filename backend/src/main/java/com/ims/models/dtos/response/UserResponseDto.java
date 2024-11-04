package com.ims.models.dtos.response;

import com.ims.models.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Integer id;
    private String username;
    private String email;
    private Role role;
}
