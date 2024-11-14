package com.ims.models.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class LoginUserDto {

        @NotEmpty
        @Size(min = 2, max = 100, message = "The length of full name must be between 2 and 100 characters.")
        private String username;
        @NotEmpty(message = "Password is required")
        @Pattern(regexp = "(?=.*[a-z])(?=.*\\d)(?=.*[@#$%])(?=.*[A-Z]).{6,16}", message = "The given password does not match the rules")
        private String password;

    public LoginUserDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

