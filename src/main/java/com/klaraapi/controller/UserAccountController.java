package com.klaraapi.controller;

import com.klaraapi.dto.UserAccountRequestDTO;
import com.klaraapi.dto.UserAccountResponseDTO;
import com.klaraapi.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "User registration and management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Operation(summary = "Register new user", description = "Creates a new user and sends a welcome message via WhatsApp")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "E-mail or phone already registered",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<UserAccountResponseDTO> create(@RequestBody @Valid UserAccountRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAccountService.create(dto));
    }

    @Operation(summary = "List all users", description = "Returns a list of all registered users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<UserAccountResponseDTO>> findAll() {
        return ResponseEntity.ok(userAccountService.findAll());
    }

    @Operation(summary = "Find user by ID", description = "Returns a single user by their ID")
    @ApiResponse(responseCode = "200", description = "User found successfully")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public ResponseEntity<UserAccountResponseDTO> findById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return ResponseEntity.ok(userAccountService.findById(id));
    }

    @Operation(summary = "Update user", description = "Updates an existing user's data")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "E-mail or phone already registered",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping("/{id}")
    public ResponseEntity<UserAccountResponseDTO> update(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody @Valid UserAccountRequestDTO dto) {
        return ResponseEntity.ok(userAccountService.update(id, dto));
    }

    @Operation(summary = "Deactivate user", description = "Deactivates a user by ID (logical deletion)")
    @ApiResponse(responseCode = "204", description = "User deactivated successfully")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userAccountService.deactivateById(id);
        return ResponseEntity.noContent().build();
    }
}
