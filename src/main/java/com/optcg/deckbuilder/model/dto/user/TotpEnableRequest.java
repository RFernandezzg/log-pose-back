package com.optcg.deckbuilder.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TotpEnableRequest {
    
    @NotBlank(message = "Secret is required")
    private String secret;
    
    @NotNull(message = "Code is required")
    private Integer code;
}
