package com.pickleball.application.usecases.user;

import com.pickleball.domain.entities.User;
import com.pickleball.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;

    @Transactional
    public User execute(Long userId, String fullName, String phoneNumber, String profilePictureUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Kiểm tra nếu số điện thoại mới đã được sử dụng bởi người khác
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            String trimmedPhone = phoneNumber.trim();
            if (!trimmedPhone.equals(user.getPhoneNumber())) {
                if (userRepository.existsByPhoneNumber(trimmedPhone)) {
                    throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
                }
                user.setPhoneNumber(trimmedPhone);
            }
        }

        // Cập nhật họ tên
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }

        // Cập nhật ảnh đại diện
        if (profilePictureUrl != null) {
            user.setProfilePictureUrl(profilePictureUrl.trim().isEmpty() ? null : profilePictureUrl.trim());
        }

        return userRepository.save(user);
    }
}
