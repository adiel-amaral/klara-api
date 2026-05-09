package com.klaraapi.controller;

import com.klaraapi.dto.UserRequestDTO;
import com.klaraapi.dto.UserResponseDTO;
import com.klaraapi.service.UserService;
import com.klaraapi.service.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Cadastro e gerenciamento de usuários")
public class UserController {

    private final UserService userService;
    private final WhatsAppService whatsAppService;

    public UserController(UserService userService, WhatsAppService whatsAppService) {
        this.userService = userService;
        this.whatsAppService = whatsAppService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo usuário", description = "Cria um novo usuário e envia mensagem de boas-vindas via WhatsApp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ou telefone já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO response = userService.create(dto);

        try {
            String greetingName = dto.socialName() != null && !dto.socialName().isBlank()
                    ? dto.socialName()
                    : dto.name();
            whatsAppService.sendWelcomeMessage(dto.phone(), greetingName);
        } catch (Exception e) {
            // Falha no WhatsApp NÃO deve reverter o cadastro
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Schema(name = "ErrorResponse")
    public record ErrorResponse(
            @Schema(example = "409") int status,
            @Schema(example = "E-mail já cadastrado") String message,
            @Schema(example = "2026-05-09T10:00:00") String timestamp
    ) {}
}
