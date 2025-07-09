package org.gaming.demo.dto;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gaming.demo.enums.UserRole;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserFullInfo {
    private Long primaryId;
    private String firstName;
    private String secondName;
    private String middleName;
    private Long userId;
    private String password;
    private String userName;
    @Enumerated(EnumType.STRING)
    private UserRole role;

}
