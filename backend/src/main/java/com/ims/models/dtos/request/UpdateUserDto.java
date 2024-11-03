package com.ims.models.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class UpdateUserDto {
    private String username;

    @Email(message = "The email address is invalid.", flags = {Pattern.Flag.CASE_INSENSITIVE })
    private String email;

    @Pattern(regexp = "(?=.*[a-z])(?=.*[d])(?=.*[@#$%])(?=.*[A-Z]).{6,16}", flags = {Pattern.Flag.CASE_INSENSITIVE}, message = "The given password does not match the rules")
    private String password;

    public UpdateUserDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
