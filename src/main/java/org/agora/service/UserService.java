package org.agora.service;

import lombok.RequiredArgsConstructor;
import org.agora.dto.UserProfileResponse;
import org.agora.entity.User;
import org.agora.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer responsible for managing user profiles and account data.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves the profile details of the currently authenticated user.
     * Extracts the user's email from the Spring Security Context to query the database.
     *
     * @return the profile response of the logged-in user.
     * @throws RuntimeException if the authenticated user cannot be found in the database.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getCreateAt()
        );
    }
}