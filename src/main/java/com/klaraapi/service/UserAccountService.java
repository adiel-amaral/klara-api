package com.klaraapi.service;

import com.klaraapi.dto.UserAccountRequestDTO;
import com.klaraapi.dto.UserAccountResponseDTO;
import com.klaraapi.entity.UserAccount;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.integration.waha.service.WahaService;
import com.klaraapi.repository.UserAccountRepository;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final WahaService wahaService;

    @Transactional
    public UserAccountResponseDTO create(UserAccountRequestDTO dto) {
        if (userAccountRepository.existsByEmail(dto.email())) {
            throw new BusinessException("E-mail already registered", HttpStatus.CONFLICT);
        }
        if (userAccountRepository.existsByPhone(dto.phone())) {
            throw new BusinessException("Phone already registered", HttpStatus.CONFLICT);
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setName(dto.name());
        userAccount.setEmail(dto.email());
        userAccount.setBirthDate(dto.birthDate());
        userAccount.setGender(dto.gender());
        userAccount.setSocialName(dto.socialName());
        userAccount.setPhone(dto.phone());

        userAccount = userAccountRepository.save(userAccount);

        sendWelcomeMessage(userAccount);

        return UserAccountResponseDTO.from(userAccount);
    }

    public List<UserAccountResponseDTO> findAll() {
        return userAccountRepository.findByActiveTrue().stream()
                .map(UserAccountResponseDTO::from)
                .toList();
    }

    public UserAccountResponseDTO findById(Long id) {
        UserAccount userAccount = userAccountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserAccountResponseDTO.from(userAccount);
    }

    @Transactional
    public UserAccountResponseDTO update(Long id, UserAccountRequestDTO dto) {
        UserAccount userAccount = userAccountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!userAccount.getEmail().equals(dto.email()) && userAccountRepository.existsByEmail(dto.email())) {
            throw new BusinessException("E-mail already registered", HttpStatus.CONFLICT);
        }
        if (!userAccount.getPhone().equals(dto.phone()) && userAccountRepository.existsByPhone(dto.phone())) {
            throw new BusinessException("Phone already registered", HttpStatus.CONFLICT);
        }

        userAccount.setName(dto.name());
        userAccount.setEmail(dto.email());
        userAccount.setBirthDate(dto.birthDate());
        userAccount.setGender(dto.gender());
        userAccount.setSocialName(dto.socialName());
        userAccount.setPhone(dto.phone());

        userAccount = userAccountRepository.save(userAccount);
        return UserAccountResponseDTO.from(userAccount);
    }

    @Transactional
    public void deactivateById(Long id) {
        UserAccount userAccount = userAccountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userAccount.setActive(false);
        userAccountRepository.save(userAccount);
    }

    private void sendWelcomeMessage(UserAccount user) {
        String name = (user.getSocialName() != null && !user.getSocialName().isBlank())
                ? user.getSocialName()
                : user.getName();
        try {
            wahaService.sendWelcomeMessage(user.getPhone(), name);
        } catch (Exception e) {
            log.warn("Could not send WhatsApp welcome message to {}: {}", user.getPhone(), e.getMessage());
        }
    }
}
