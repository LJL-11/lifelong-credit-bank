package org.csu.creditbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResult {

    private String token;
    private Long learnerId;
    private String username;
    private String realName;
    private String role;
    private String institutionName;
}
