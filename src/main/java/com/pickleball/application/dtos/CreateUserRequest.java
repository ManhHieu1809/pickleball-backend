package com.pickleball.application.dtos;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email phải hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Tên đầy đủ là bắt buộc")
    @Size(max = 50, message = "Tên đầy đủ không được vượt quá 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Số điện thoại chứa ký tự không hợp lệs")
    private String phoneNumber;
}