package com.ims.models.dtos.response;

import com.ims.models.Role;
import com.ims.models.User;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String email;
    private String username;
    private Role role;
}