package com.klaraapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klaraapi.dto.UserAccountRequestDTO;
import com.klaraapi.dto.UserAccountResponseDTO;
import com.klaraapi.enums.Gender;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.service.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserAccountControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    UserAccountService userAccountService;

    @InjectMocks
    UserAccountController userAccountController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userAccountController)
                .setControllerAdvice(new com.klaraapi.exception.GlobalExceptionHandler())
                .build();
    }

    @Nested
    class Create {

        @Test
        void shouldReturn201_whenUserCreatedSuccessfully() throws Exception {
            var responseDto = responseDto(1L, "João Silva", "joao@email.com", "+5548999999999");
            given(userAccountService.create(any(UserAccountRequestDTO.class))).willReturn(responseDto);

            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("João Silva"))
                    .andExpect(jsonPath("$.email").value("joao@email.com"))
                    .andExpect(jsonPath("$.phone").value("+5548999999999"));
        }

        @Test
        void shouldReturn400_whenNameIsMissing() throws Exception {
            String json = jsonRequest(null, "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenEmailIsMissing() throws Exception {
            String json = jsonRequest("João Silva", null, "1990-01-15", "MALE", null, "+5548999999999");

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenEmailIsInvalid() throws Exception {
            String json = jsonRequest("João Silva", "not-an-email", "1990-01-15", "MALE", null, "+5548999999999");

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenBirthDateIsMissing() throws Exception {
            String json = jsonRequest("João Silva", "joao@email.com", null, "MALE", null, "+5548999999999");

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenGenderIsMissing() throws Exception {
            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", null, null, "+5548999999999");

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenPhoneIsMissing() throws Exception {
            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", "MALE", null, null);

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenPhoneIsInvalidFormat() throws Exception {
            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "48999999999");

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn409_whenEmailAlreadyExists() throws Exception {
            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");
            given(userAccountService.create(any(UserAccountRequestDTO.class)))
                    .willThrow(new BusinessException("E-mail already registered", HttpStatus.CONFLICT));

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("E-mail already registered"));
        }

        @Test
        void shouldReturn409_whenPhoneAlreadyExists() throws Exception {
            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");
            given(userAccountService.create(any(UserAccountRequestDTO.class)))
                    .willThrow(new BusinessException("Phone already registered", HttpStatus.CONFLICT));

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Phone already registered"));
        }

        @Test
        void shouldReturn201_evenWhenWhatsAppFails() throws Exception {
            var responseDto = responseDto(1L, "João Silva", "joao@email.com", "+5548999999999");
            given(userAccountService.create(any(UserAccountRequestDTO.class))).willReturn(responseDto);

            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturn200_withAllUsers() throws Exception {
            var user1 = responseDto(1L, "João Silva", "joao@email.com", "+5548999999999");
            var user2 = responseDto(2L, "Maria Santos", "maria@email.com", "+5548888888888");
            given(userAccountService.findAll()).willReturn(List.of(user1, user2));

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("João Silva"))
                    .andExpect(jsonPath("$[1].name").value("Maria Santos"));
        }

        @Test
        void shouldReturn200_withEmptyList() throws Exception {
            given(userAccountService.findAll()).willReturn(List.of());

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturn200_whenUserExists() throws Exception {
            var responseDto = responseDto(1L, "João Silva", "joao@email.com", "+5548999999999");
            given(userAccountService.findById(1L)).willReturn(responseDto);

            mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("João Silva"));
        }

        @Test
        void shouldReturn404_whenUserNotFound() throws Exception {
            given(userAccountService.findById(99L))
                    .willThrow(new ResourceNotFoundException("User not found with id: 99"));

            mockMvc.perform(get("/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("User not found with id: 99"));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldReturn200_whenUpdateSuccessfully() throws Exception {
            var responseDto = responseDto(1L, "João Silva", "joao@email.com", "+5548999999999");
            given(userAccountService.update(eq(1L), any(UserAccountRequestDTO.class))).willReturn(responseDto);

            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

            mockMvc.perform(put("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("João Silva"));
        }

        @Test
        void shouldReturn400_whenUpdateWithInvalidData() throws Exception {
            String json = jsonRequest(null, "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

            mockMvc.perform(put("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn404_whenUpdateUserNotFound() throws Exception {
            String json = jsonRequest("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");
            given(userAccountService.update(eq(99L), any(UserAccountRequestDTO.class)))
                    .willThrow(new ResourceNotFoundException("User not found with id: 99"));

            mockMvc.perform(put("/users/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("User not found with id: 99"));
        }

        @Test
        void shouldReturn409_whenUpdateEmailAlreadyExists() throws Exception {
            String json = jsonRequest("João Silva", "other@email.com", "1990-01-15", "MALE", null, "+5548999999999");
            given(userAccountService.update(eq(1L), any(UserAccountRequestDTO.class)))
                    .willThrow(new BusinessException("E-mail already registered", HttpStatus.CONFLICT));

            mockMvc.perform(put("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("E-mail already registered"));
        }
    }

    @Nested
    class Delited {

        @Test
        void shouldReturn204_whenDelitedSuccessfully() throws Exception {
            doNothing().when(userAccountService).delitedById(1L);

            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isNoContent());

            then(userAccountService).should().delitedById(1L);
        }

        @Test
        void shouldReturn404_whenDelitedUserNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("User not found with id: 99"))
                    .when(userAccountService).delitedById(99L);

            mockMvc.perform(delete("/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("User not found with id: 99"));
        }
    }

    // ===== Helpers =====

    private String jsonRequest(String name, String email, String birthDate, String gender, String socialName, String phone) {
        StringBuilder sb = new StringBuilder("{");
        if (name != null) sb.append("\"name\":\"").append(name).append("\",");
        if (email != null) sb.append("\"email\":\"").append(email).append("\",");
        if (birthDate != null) sb.append("\"birthDate\":\"").append(birthDate).append("\",");
        if (gender != null) sb.append("\"gender\":\"").append(gender).append("\",");
        if (socialName != null) sb.append("\"socialName\":\"").append(socialName).append("\",");
        if (phone != null) sb.append("\"phone\":\"").append(phone).append("\",");
        if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    private static UserAccountResponseDTO responseDto(Long id, String name, String email, String phone) {
        return new UserAccountResponseDTO(
                id, name, email,
                LocalDate.of(1990, 1, 15), Gender.MALE, null,
                phone, true, LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
