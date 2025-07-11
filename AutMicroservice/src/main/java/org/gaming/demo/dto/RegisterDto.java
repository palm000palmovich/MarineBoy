package org.gaming.demo.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterDto {
    private String firstName;
    private String secondName;
    private String middleName;
    @NotNull(message = "Поле password должно быть заполнено.")
    private String password;
    @NotNull(message = "Поле userName должно быть заполнено.")
    private String userName;
}