package com.klaraapi.service;

import com.klaraapi.dto.UserRequestDTO;
import com.klaraapi.dto.UserResponseDTO;
import com.klaraapi.entity.UserProfile;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserProfileRepository userProfileRepository;
    private final WhatsAppService whatsAppService;

    public UserService(UserProfileRepository userProfileRepository, WhatsAppService whatsAppService) {
        this.userProfileRepository = userProfileRepository;
        this.whatsAppService = whatsAppService;
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userProfileRepository.existsByEmail(dto.email())) {
            throw new BusinessException("E-mail já cadastrado", HttpStatus.CONFLICT);
        }
        if (userProfileRepository.existsByPhone(dto.phone())) {
            throw new BusinessException("Telefone já cadastrado", HttpStatus.CONFLICT);
        }
        if (userProfileRepository.existsByCpf(dto.cpf())) {
            throw new BusinessException("CPF já cadastrado", HttpStatus.CONFLICT);
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
            log.warn("Não foi possível enviar mensagem de boas-vindas via WhatsApp para {}: {}", user.getPhone(), e.getMessage());
        }
    }
}
