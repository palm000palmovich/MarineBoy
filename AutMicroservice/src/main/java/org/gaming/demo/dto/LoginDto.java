package org.gaming.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginDto {
    @NotNull(message = "Поле userName должно быть заполнено.")
    private String userName;
    @NotNull(message = "Поле password должно быть заполнено.")
    private String password;
}
