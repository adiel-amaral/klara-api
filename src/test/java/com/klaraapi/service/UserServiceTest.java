package com.klaraapi.service;

import com.klaraapi.dto.UserRequestDTO;
import com.klaraapi.dto.UserResponseDTO;
import com.klaraapi.entity.UserProfile;
import com.klaraapi.enums.Gender;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserService userService;

    private UserRequestDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new UserRequestDTO(
                "João Silva",
                "joao@email.com",
                LocalDate.of(1990, 1, 15),
                Gender.MALE,
                null,
                "+5548999999999"
        );
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userProfileRepository.existsByEmail(validDto.email())).thenReturn(false);
        when(userProfileRepository.existsByPhone(validDto.phone())).thenReturn(false);

        UserProfile savedUserProfile = new UserProfile();
        savedUserProfile.setId(1L);
        savedUserProfile.setName(validDto.name());
        savedUserProfile.setEmail(validDto.email());
        savedUserProfile.setBirthDate(validDto.birthDate());
        savedUserProfile.setGender(validDto.gender());
        savedUserProfile.setSocialName(validDto.socialName());
        savedUserProfile.setPhone(validDto.phone());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(savedUserProfile);

        UserResponseDTO response = userService.create(validDto);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("João Silva");
        assertThat(response.email()).isEqualTo("joao@email.com");
        assertThat(response.phone()).isEqualTo("+5548999999999");

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("João Silva");
    }

    @Test
    void shouldCreateUserWithSocialName() {
        UserRequestDTO dtoWithSocialName = new UserRequestDTO(
                "João Silva",
                "joao@email.com",
                LocalDate.of(1990, 1, 15),
                Gender.MALE,
                "João",
                "+5548999999999"
        );

        when(userProfileRepository.existsByEmail(dtoWithSocialName.email())).thenReturn(false);
        when(userProfileRepository.existsByPhone(dtoWithSocialName.phone())).thenReturn(false);

        UserProfile savedUserProfile = new UserProfile();
        savedUserProfile.setId(1L);
        savedUserProfile.setName(dtoWithSocialName.name());
        savedUserProfile.setEmail(dtoWithSocialName.email());
        savedUserProfile.setBirthDate(dtoWithSocialName.birthDate());
        savedUserProfile.setGender(dtoWithSocialName.gender());
        savedUserProfile.setSocialName(dtoWithSocialName.socialName());
        savedUserProfile.setPhone(dtoWithSocialName.phone());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(savedUserProfile);

        UserResponseDTO response = userService.create(dtoWithSocialName);

        assertThat(response.socialName()).isEqualTo("João");
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        when(userProfileRepository.existsByEmail(validDto.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(validDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("E-mail já cadastrado");

        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenPhoneAlreadyExists() {
        when(userProfileRepository.existsByEmail(validDto.email())).thenReturn(false);
        when(userProfileRepository.existsByPhone(validDto.phone())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(validDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Telefone já cadastrado");

        verify(userProfileRepository, never()).save(any());
    }
}
