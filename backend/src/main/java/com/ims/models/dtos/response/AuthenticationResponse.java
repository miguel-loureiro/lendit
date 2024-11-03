package com.ims.models.dtos.response;

import com.ims.models.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse {

    private User user;
    private String token;

    public AuthenticationResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }
}
