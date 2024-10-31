package com.ims.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
public class LoginUserRequestDto {

        @Getter
        @Setter
        @Size(min = 2, max = 100, message = "The length of full name must be between 2 and 100 characters.")
        private String username;
        @Getter
        @Setter
        @NotEmpty(message = "Email is required")
        @Email(message = "The email address is invalid.", flags = {Pattern.Flag.CASE_INSENSITIVE})
        private String email;
        @Getter
        @Setter
        @NotEmpty(message = "Password is required")
        @Pattern(regexp = "(?=.*[a-z])(?=.*\\d)(?=.*[@#$%])(?=.*[A-Z]).{6,16}", message = "The given password does not match the rules")
        private String password;

    public LoginUserRequestDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}

