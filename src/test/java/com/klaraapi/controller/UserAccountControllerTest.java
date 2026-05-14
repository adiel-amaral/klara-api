package com.klaraapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klaraapi.dto.UserAccountRequestDTO;
import com.klaraapi.dto.UserAccountResponseDTO;
import com.klaraapi.enums.Gender;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.service.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserAccountControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private UserAccountController userAccountController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userAccountController)
                .setControllerAdvice(new com.klaraapi.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private String buildJson(String name, String email, String birthDate, String gender, String socialName, String phone) {
        StringBuilder sb = new StringBuilder("{");
        if (name != null) sb.append("\"name\":\"").append(name).append("\",");
        if (email != null) sb.append("\"email\":\"").append(email).append("\",");
        if (birthDate != null) sb.append("\"birthDate\":\"").append(birthDate).append("\",");
        if (gender != null) sb.append("\"gender\":\"").append(gender).append("\",");
        if (socialName != null) sb.append("\"socialName\":\"").append(socialName).append("\",");
        if (phone != null) sb.append("\"phone\":\"").append(phone).append("\",");
        // Remove trailing comma
        if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    @Test
    void shouldReturn201WhenUserCreatedSuccessfully() throws Exception {
        UserAccountResponseDTO responseDto = new UserAccountResponseDTO(
                1L, "João Silva", "joao@email.com",
                LocalDate.of(1990, 1, 15), Gender.MALE, null,
                "+5548999999999", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(userAccountService.create(any(UserAccountRequestDTO.class))).thenReturn(responseDto);

        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

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
    void shouldReturn400WhenNameIsMissing() throws Exception {
        String json = buildJson(null, "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsMissing() throws Exception {
        String json = buildJson("João Silva", null, "1990-01-15", "MALE", null, "+5548999999999");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        String json = buildJson("João Silva", "not-an-email", "1990-01-15", "MALE", null, "+5548999999999");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBirthDateIsMissing() throws Exception {
        String json = buildJson("João Silva", "joao@email.com", null, "MALE", null, "+5548999999999");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenGenderIsMissing() throws Exception {
        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", null, null, "+5548999999999");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPhoneIsMissing() throws Exception {
        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPhoneIsInvalidFormat() throws Exception {
        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "48999999999");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");
        when(userAccountService.create(any(UserAccountRequestDTO.class)))
                .thenThrow(new BusinessException("E-mail already registered", HttpStatus.CONFLICT));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("E-mail already registered"));
    }

    @Test
    void shouldReturn409WhenPhoneAlreadyExists() throws Exception {
        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");
        when(userAccountService.create(any(UserAccountRequestDTO.class)))
                .thenThrow(new BusinessException("Phone already registered", HttpStatus.CONFLICT));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Phone already registered"));
    }

    @Test
    void shouldReturn201EvenWhenWhatsAppFails() throws Exception {
        UserAccountResponseDTO responseDto = new UserAccountResponseDTO(
                1L, "João Silva", "joao@email.com",
                LocalDate.of(1990, 1, 15), Gender.MALE, null,
                "+5548999999999", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(userAccountService.create(any(UserAccountRequestDTO.class))).thenReturn(responseDto);

        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // --- findAll ---

    @Test
    void shouldReturn200WithAllUsers() throws Exception {
        UserAccountResponseDTO user1 = new UserAccountResponseDTO(
                1L, "João Silva", "joao@email.com",
                LocalDate.of(1990, 1, 15), Gender.MALE, null,
                "+5548999999999", true, LocalDateTime.now(), LocalDateTime.now()
        );
        UserAccountResponseDTO user2 = new UserAccountResponseDTO(
                2L, "Maria Santos", "maria@email.com",
                LocalDate.of(1995, 8, 20), Gender.FEMALE, null,
                "+5548888888888", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(userAccountService.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("João Silva"))
                .andExpect(jsonPath("$[1].name").value("Maria Santos"));
    }

    @Test
    void shouldReturn200WithEmptyList() throws Exception {
        when(userAccountService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- findById ---

    @Test
    void shouldReturn200WhenFindByIdExists() throws Exception {
        UserAccountResponseDTO responseDto = new UserAccountResponseDTO(
                1L, "João Silva", "joao@email.com",
                LocalDate.of(1990, 1, 15), Gender.MALE, null,
                "+5548999999999", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(userAccountService.findById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João Silva"));
    }

    @Test
    void shouldReturn404WhenFindByIdDoesNotExist() throws Exception {
        when(userAccountService.findById(99L))
                .thenThrow(new com.klaraapi.exception.ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found with id: 99"));
    }

    // --- update ---

    @Test
    void shouldReturn200WhenUpdateSuccessfully() throws Exception {
        UserAccountResponseDTO responseDto = new UserAccountResponseDTO(
                1L, "João Silva", "joao@email.com",
                LocalDate.of(1990, 1, 15), Gender.MALE, null,
                "+5548999999999", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(userAccountService.update(eq(1L), any(UserAccountRequestDTO.class))).thenReturn(responseDto);

        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João Silva"));
    }

    @Test
    void shouldReturn400WhenUpdateWithInvalidData() throws Exception {
        String json = buildJson(null, "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUpdateDoesNotExist() throws Exception {
        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");
        when(userAccountService.update(eq(99L), any(UserAccountRequestDTO.class)))
                .thenThrow(new com.klaraapi.exception.ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(put("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found with id: 99"));
    }

    @Test
    void shouldReturn409WhenUpdateEmailAlreadyExists() throws Exception {
        String json = buildJson("João Silva", "other@email.com", "1990-01-15", "MALE", null, "+5548999999999");
        when(userAccountService.update(eq(1L), any(UserAccountRequestDTO.class)))
                .thenThrow(new BusinessException("E-mail already registered", HttpStatus.CONFLICT));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("E-mail already registered"));
    }

    // --- deactivate ---

    @Test
    void shouldReturn204WhenDeactivateSuccessfully() throws Exception {
        doNothing().when(userAccountService).deactivateById(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userAccountService).deactivateById(1L);
    }

    @Test
    void shouldReturn404WhenDeactivateDoesNotExist() throws Exception {
        doThrow(new com.klaraapi.exception.ResourceNotFoundException("User not found with id: 99"))
                .when(userAccountService).deactivateById(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found with id: 99"));
    }
}
