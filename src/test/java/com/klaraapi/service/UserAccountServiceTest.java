package com.klaraapi.service;

import com.klaraapi.dto.UserAccountRequestDTO;
import com.klaraapi.dto.UserAccountResponseDTO;
import com.klaraapi.entity.UserAccount;
import com.klaraapi.enums.Gender;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.integration.waha.service.WahaService;
import com.klaraapi.repository.UserAccountRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    UserAccountRepository userAccountRepository;

    @Mock
    WahaService wahaService;

    @InjectMocks
    UserAccountService userAccountService;

    @Nested
    class Create {

        @Test
        void shouldReturnDTO_whenEmailAndPhoneAreUnique() {
            var request = new UserAccountRequestDTO("João Silva", "joao@email.com",
                    LocalDate.of(1990, 1, 15), Gender.MALE, null, "+5548999999999");
            given(userAccountRepository.existsByEmail("joao@email.com")).willReturn(false);
            given(userAccountRepository.existsByPhone("+5548999999999")).willReturn(false);
            given(userAccountRepository.save(any())).willAnswer(inv -> withId(inv.getArgument(0), 1L));

            var response = userAccountService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("João Silva");
            assertThat(response.email()).isEqualTo("joao@email.com");
            assertThat(response.active()).isTrue();
            then(userAccountRepository).should().save(any());
        }

        @Test
        void shouldThrowBusinessException_whenEmailAlreadyExists() {
            var request = new UserAccountRequestDTO("João Silva", "joao@email.com",
                    LocalDate.of(1990, 1, 15), Gender.MALE, null, "+5548999999999");
            given(userAccountRepository.existsByEmail("joao@email.com")).willReturn(true);

            assertThatThrownBy(() -> userAccountService.create(request))
                    .isInstanceOf(BusinessException.class);
            then(userAccountRepository).should(never()).save(any());
        }

        @Test
        void shouldThrowBusinessException_whenPhoneAlreadyExists() {
            var request = new UserAccountRequestDTO("João Silva", "joao@email.com",
                    LocalDate.of(1990, 1, 15), Gender.MALE, null, "+5548999999999");
            given(userAccountRepository.existsByEmail("joao@email.com")).willReturn(false);
            given(userAccountRepository.existsByPhone("+5548999999999")).willReturn(true);

            assertThatThrownBy(() -> userAccountService.create(request))
                    .isInstanceOf(BusinessException.class);
            then(userAccountRepository).should(never()).save(any());
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnListOfActiveUsers() {
            var user1 = withId(user("João Silva", "joao@email.com", "+5548999999999"), 1L);
            var user2 = withId(user("Maria Santos", "maria@email.com", "+5548988888888"), 2L);
            given(userAccountRepository.findByActiveTrue()).willReturn(List.of(user1, user2));

            var responses = userAccountService.findAll();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).name()).isEqualTo("João Silva");
            assertThat(responses.get(1).name()).isEqualTo("Maria Santos");
        }

        @Test
        void shouldReturnEmptyList_whenNoActiveUsers() {
            given(userAccountRepository.findByActiveTrue()).willReturn(List.of());

            var responses = userAccountService.findAll();

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnDTO_whenUserExistsAndIsActive() {
            var user = withId(user("João Silva", "joao@email.com", "+5548999999999"), 1L);
            given(userAccountRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(user));

            var response = userAccountService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("João Silva");
        }

        @Test
        void shouldThrowResourceNotFoundException_whenUserNotFound() {
            given(userAccountRepository.findByIdAndActiveTrue(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userAccountService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class Update {

        @Test
        void shouldReturnDTO_whenUserExistsAndEmailAndPhoneAreUnique() {
            var existing = withId(user("João Silva", "joao@email.com", "+5548999999999"), 1L);
            var request = new UserAccountRequestDTO("João Silva Updated", "joao@email.com",
                    LocalDate.of(1990, 1, 15), Gender.MALE, null, "+5548999999999");
            given(userAccountRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(existing));
            given(userAccountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            var response = userAccountService.update(1L, request);

            assertThat(response).isNotNull();
            then(userAccountRepository).should().save(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenUserNotFound() {
            var request = new UserAccountRequestDTO("Test", "test@email.com",
                    LocalDate.of(1990, 1, 15), Gender.MALE, null, "+5548999999999");
            given(userAccountRepository.findByIdAndActiveTrue(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userAccountService.update(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            then(userAccountRepository).should(never()).save(any());
        }

        @Test
        void shouldThrowBusinessException_whenUpdatingWithDuplicateEmail() {
            var existing = withId(user("João Silva", "joao@email.com", "+5548999999999"), 1L);
            var request = new UserAccountRequestDTO("João Silva", "maria@email.com",
                    LocalDate.of(1990, 1, 15), Gender.MALE, null, "+5548999999999");
            given(userAccountRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(existing));
            given(userAccountRepository.existsByEmail("maria@email.com")).willReturn(true);

            assertThatThrownBy(() -> userAccountService.update(1L, request))
                    .isInstanceOf(BusinessException.class);
            then(userAccountRepository).should(never()).save(any());
        }

        @Test
        void shouldThrowBusinessException_whenUpdatingWithDuplicatePhone() {
            var existing = withId(user("João Silva", "joao@email.com", "+5548999999999"), 1L);
            var request = new UserAccountRequestDTO("João Silva", "joao@email.com",
                    LocalDate.of(1990, 1, 15), Gender.MALE, null, "+5548988888888");
            given(userAccountRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(existing));
            given(userAccountRepository.existsByPhone("+5548988888888")).willReturn(true);

            assertThatThrownBy(() -> userAccountService.update(1L, request))
                    .isInstanceOf(BusinessException.class);
            then(userAccountRepository).should(never()).save(any());
        }
    }

    @Nested
    class Delited {

        @Test
        void shouldDelited_whenUserExistsAndIsActive() {
            var user = withId(user("João Silva", "joao@email.com", "+5548999999999"), 1L);
            given(userAccountRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(user));
            given(userAccountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            userAccountService.delitedById(1L);

            then(userAccountRepository).should().save(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenUserNotFound() {
            given(userAccountRepository.findByIdAndActiveTrue(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userAccountService.delitedById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
            then(userAccountRepository).should(never()).save(any());
        }
    }

    // ===== Helpers =====

    private static UserAccount withId(UserAccount user, Long id) {
        user.setId(id);
        user.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return user;
    }

    private static UserAccount user(String name, String email, String phone) {
        var user = new UserAccount();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setBirthDate(LocalDate.of(1990, 1, 15));
        user.setGender(Gender.MALE);
        user.setActive(true);
        return user;
    }
}
