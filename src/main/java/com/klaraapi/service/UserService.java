package com.klaraapi.service;

import com.klaraapi.dto.UserRequestDTO;
import com.klaraapi.dto.UserResponseDTO;
import com.klaraapi.entity.UserProfile;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userProfileRepository.existsByEmail(dto.email())) {
            throw new BusinessException("E-mail já cadastrado", HttpStatus.CONFLICT);
        }
        if (userProfileRepository.existsByPhone(dto.phone())) {
            throw new BusinessException("Telefone já cadastrado", HttpStatus.CONFLICT);
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setName(dto.name());
        userProfile.setEmail(dto.email());
        userProfile.setBirthDate(dto.birthDate());
        userProfile.setGender(dto.gender());
        userProfile.setSocialName(dto.socialName());
        userProfile.setPhone(dto.phone());

        userProfile = userProfileRepository.save(userProfile);
        return UserResponseDTO.from(userProfile);
    }
}
