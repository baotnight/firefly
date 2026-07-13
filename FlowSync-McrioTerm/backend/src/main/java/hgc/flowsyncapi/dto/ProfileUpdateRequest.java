package hgc.flowsyncapi.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String phone;
    private String email;
}
