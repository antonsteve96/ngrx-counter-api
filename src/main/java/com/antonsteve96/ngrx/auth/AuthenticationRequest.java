package com.antonsteve96.ngrx.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {

    @Email(message = "L'email non è ben formattata")
    @NotEmpty(message = "L'email è obbligatoria")
    @NotNull(message = "L'email è obbligatoria")
    private String email;

    @NotEmpty(message = "La password è obbligatoria")
    @NotNull(message = "La password è obbligatoria")
    @Size(min = 8, message = "La password dovrebbe essere lunga 8 caratteri almeno")
    private String password;
}

