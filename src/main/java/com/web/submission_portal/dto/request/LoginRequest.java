package com.web.submission_portal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LoginRequest {

    @NotEmpty(message = "Email is required")
    @Email(message = "Email is incorrect")
    private String email;

    @NotEmpty(message = "Password field can't be empty")
    @Size(message = "Password must contains 6 characters")
    private String password;
}
