package com.klaraapi.service;

import com.klaraapi.dto.UserRequestDTO;
import com.klaraapi.dto.UserResponseDTO;
import com.klaraapi.entity.UserProfile;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final WhatsAppService whatsAppService;

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userProfileRepository.existsByEmail(dto.email())) {
            throw new BusinessException("E-mail already registered", HttpStatus.CONFLICT);
        }
        if (userProfileRepository.existsByPhone(dto.phone())) {
            throw new BusinessException("Phone already registered", HttpStatus.CONFLICT);
        }
        if (userProfileRepository.existsByCpf(dto.cpf())) {
            throw new BusinessException("CPF already registered", HttpStatus.CONFLICT);
        }
        UserProfile userProfile = new UserProfile();
        userProfile.setName(dto.name());
        userProfile.setEmail(dto.email());
        userProfile.setCpf(dto.cpf());
        userProfile.setBirthDate(dto.birthDate());
        userProfile.setGender(dto.gender());
        userProfile.setSocialName(dto.socialName());
        userProfile.setPhone(dto.phone());

        userProfile = userProfileRepository.save(userProfile);

        sendWelcomeMessage(userProfile);

        return UserResponseDTO.from(userProfile);
    }

    private void sendWelcomeMessage(UserProfile user) {
        String name = (user.getSocialName() != null && !user.getSocialName().isBlank())
                ? user.getSocialName()
                : user.getName();
        try {
            whatsAppService.sendWelcomeMessage(user.getPhone(), name);
        } catch (Exception e) {
            log.warn("Could not send WhatsApp welcome message to {}: {}", user.getPhone(), e.getMessage());
        }
    }
}
