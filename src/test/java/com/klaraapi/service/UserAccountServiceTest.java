package com.klaraapi.service;

import com.klaraapi.dto.UserAccountRequestDTO;
import com.klaraapi.dto.UserAccountResponseDTO;
import com.klaraapi.entity.UserAccount;
import com.klaraapi.enums.Gender;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.integration.waha.service.WahaService;
import com.klaraapi.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private WahaService wahaService;

    @InjectMocks
    private UserAccountService userAccountService;

    private UserAccountRequestDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new UserAccountRequestDTO(
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
        when(userAccountRepository.existsByEmail(validDto.email())).thenReturn(false);
        when(userAccountRepository.existsByPhone(validDto.phone())).thenReturn(false);

        UserAccount savedUserAccount = new UserAccount();
        savedUserAccount.setId(1L);
        savedUserAccount.setName(validDto.name());
        savedUserAccount.setEmail(validDto.email());
        savedUserAccount.setBirthDate(validDto.birthDate());
        savedUserAccount.setGender(validDto.gender());
        savedUserAccount.setSocialName(validDto.socialName());
        savedUserAccount.setPhone(validDto.phone());
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUserAccount);

        UserAccountResponseDTO response = userAccountService.create(validDto);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("João Silva");
        assertThat(response.email()).isEqualTo("joao@email.com");
        assertThat(response.phone()).isEqualTo("+5548999999999");

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("João Silva");
    }

    @Test
    void shouldCreateUserWithSocialName() {
        UserAccountRequestDTO dtoWithSocialName = new UserAccountRequestDTO(
                "João Silva",
                "joao@email.com",
                LocalDate.of(1990, 1, 15),
                Gender.MALE,
                "João",
                "+5548999999999"
        );

        when(userAccountRepository.existsByEmail(dtoWithSocialName.email())).thenReturn(false);
        when(userAccountRepository.existsByPhone(dtoWithSocialName.phone())).thenReturn(false);

        UserAccount savedUserAccount = new UserAccount();
        savedUserAccount.setId(1L);
        savedUserAccount.setName(dtoWithSocialName.name());
        savedUserAccount.setEmail(dtoWithSocialName.email());
        savedUserAccount.setBirthDate(dtoWithSocialName.birthDate());
        savedUserAccount.setGender(dtoWithSocialName.gender());
        savedUserAccount.setSocialName(dtoWithSocialName.socialName());
        savedUserAccount.setPhone(dtoWithSocialName.phone());
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUserAccount);

        UserAccountResponseDTO response = userAccountService.create(dtoWithSocialName);

        assertThat(response.socialName()).isEqualTo("João");
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        when(userAccountRepository.existsByEmail(validDto.email())).thenReturn(true);

        assertThatThrownBy(() -> userAccountService.create(validDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("E-mail already registered");

        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenPhoneAlreadyExists() {
        when(userAccountRepository.existsByEmail(validDto.email())).thenReturn(false);
        when(userAccountRepository.existsByPhone(validDto.phone())).thenReturn(true);

        assertThatThrownBy(() -> userAccountService.create(validDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Phone already registered");

        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllActiveUsers() {
        UserAccount user1 = buildUserAccount(1L, "João Silva", "joao@email.com");
        UserAccount user2 = buildUserAccount(2L, "Maria Santos", "maria@email.com");
        when(userAccountRepository.findByActiveTrue()).thenReturn(List.of(user1, user2));

        List<UserAccountResponseDTO> result = userAccountService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("João Silva");
        assertThat(result.get(1).name()).isEqualTo("Maria Santos");
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveUsers() {
        when(userAccountRepository.findByActiveTrue()).thenReturn(List.of());

        List<UserAccountResponseDTO> result = userAccountService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindActiveUserById() {
        UserAccount user = buildUserAccount(1L, "João Silva", "joao@email.com");
        when(userAccountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(user));

        UserAccountResponseDTO result = userAccountService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("João Silva");
        assertThat(result.active()).isTrue();
    }

    @Test
    void shouldThrowNotFoundWhenFindByIdDoesNotExist() {
        when(userAccountRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAccountService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 99");
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        UserAccount existing = buildUserAccount(1L, "João Silva", "joao@email.com");
        when(userAccountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));

        UserAccount updated = new UserAccount();
        updated.setId(1L);
        updated.setName(validDto.name());
        updated.setEmail(validDto.email());
        updated.setBirthDate(validDto.birthDate());
        updated.setGender(validDto.gender());
        updated.setSocialName(validDto.socialName());
        updated.setPhone(validDto.phone());
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(updated);

        UserAccountResponseDTO result = userAccountService.update(1L, validDto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("João Silva");
        assertThat(result.email()).isEqualTo("joao@email.com");
    }

    @Test
    void shouldThrowNotFoundWhenUpdateDoesNotExist() {
        when(userAccountRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAccountService.update(99L, validDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 99");
    }

    @Test
    void shouldThrowConflictWhenUpdateEmailAlreadyExists() {
        UserAccount existing = buildUserAccount(1L, "João Silva", "joao@email.com");
        UserAccountRequestDTO newDto = new UserAccountRequestDTO(
                "João Silva", "other@email.com", LocalDate.of(1990, 1, 15),
                Gender.MALE, null, "+5548999999999"
        );

        when(userAccountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.existsByEmail("other@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userAccountService.update(1L, newDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("E-mail already registered");
    }

    @Test
    void shouldThrowConflictWhenUpdatePhoneAlreadyExists() {
        UserAccount existing = buildUserAccount(1L, "João Silva", "joao@email.com");
        UserAccountRequestDTO newDto = new UserAccountRequestDTO(
                "João Silva", "joao@email.com", LocalDate.of(1990, 1, 15),
                Gender.MALE, null, "+5548111111111"
        );

        when(userAccountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.existsByPhone("+5548111111111")).thenReturn(true);

        assertThatThrownBy(() -> userAccountService.update(1L, newDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Phone already registered");
    }

    @Test
    void shouldDeactivateUserById() {
        UserAccount user = buildUserAccount(1L, "João Silva", "joao@email.com");
        when(userAccountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(user);

        userAccountService.deactivateById(1L);

        assertThat(user.isActive()).isFalse();
        verify(userAccountRepository).save(user);
    }

    @Test
    void shouldThrowNotFoundWhenDeactivateDoesNotExist() {
        when(userAccountRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAccountService.deactivateById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 99");

        verify(userAccountRepository, never()).save(any());
    }

    private UserAccount buildUserAccount(Long id, String name, String email) {
        UserAccount user = new UserAccount();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setBirthDate(LocalDate.of(1990, 1, 15));
        user.setGender(Gender.MALE);
        user.setPhone("+5548999999999");
        return user;
    }
}
