package com.ims.models.dtos.response;

import com.ims.models.Role;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private Integer id;
    private String email;
    private String username;
    private Role role;
}
