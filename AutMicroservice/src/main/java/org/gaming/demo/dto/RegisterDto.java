package org.gaming.demo.dto;

import jakarta.persistence.Column;
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
    private String password;
    private String userName;
}