package com.example.demo.dto.request;

import jakarta.validation.constraints.*;

public class ReceptionistRequest {
    @NotBlank @Size(max = 150)
    private String fullName;

    @NotBlank @Email
    private String email;

    @NotBlank @Pattern(regexp = "^[+0-9 \\-]{7,20}$", message = "Invalid phone")
    private String phone;

    @NotBlank @Size(min = 6, max = 80)
    private String password;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
