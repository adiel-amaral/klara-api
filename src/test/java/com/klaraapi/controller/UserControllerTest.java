package com.klaraapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klaraapi.dto.UserRequestDTO;
import com.klaraapi.dto.UserResponseDTO;
import com.klaraapi.enums.Gender;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.service.UserService;
import com.klaraapi.service.WhatsAppService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
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
        UserResponseDTO responseDto = new UserResponseDTO(
                1L, "João Silva", "joao@email.com",
                LocalDate.of(1990, 1, 15), Gender.MALE, null,
                "+5548999999999", LocalDateTime.now()
        );
        when(userService.create(any(UserRequestDTO.class))).thenReturn(responseDto);

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
        when(userService.create(any(UserRequestDTO.class)))
                .thenThrow(new BusinessException("E-mail já cadastrado", HttpStatus.CONFLICT));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void shouldReturn409WhenPhoneAlreadyExists() throws Exception {
        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");
        when(userService.create(any(UserRequestDTO.class)))
                .thenThrow(new BusinessException("Telefone já cadastrado", HttpStatus.CONFLICT));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Telefone já cadastrado"));
    }

    @Test
    void shouldReturn201EvenWhenWhatsAppFails() throws Exception {
        UserResponseDTO responseDto = new UserResponseDTO(
                1L, "João Silva", "joao@email.com",
                LocalDate.of(1990, 1, 15), Gender.MALE, null,
                "+5548999999999", LocalDateTime.now()
        );
        when(userService.create(any(UserRequestDTO.class))).thenReturn(responseDto);
        doThrow(new RuntimeException("WAHA unavailable"))
                .when(whatsAppService).sendWelcomeMessage(any(), any());

        String json = buildJson("João Silva", "joao@email.com", "1990-01-15", "MALE", null, "+5548999999999");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
