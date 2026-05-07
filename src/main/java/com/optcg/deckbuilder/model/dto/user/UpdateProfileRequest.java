package com.optcg.deckbuilder.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Size(max = 255, message = "Avatar URL is too long")
    private String avatarUrl;
    
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String currentPassword;
    
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String newPassword;
}
