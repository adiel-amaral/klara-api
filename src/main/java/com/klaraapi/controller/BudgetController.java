package com.klaraapi.controller;

import com.klaraapi.dto.BudgetOverviewDTO;
import com.klaraapi.dto.BudgetRequestDTO;
import com.klaraapi.dto.BudgetResponseDTO;
import com.klaraapi.service.BudgetService;
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

@Tag(name = "Budgets", description = "Monthly budget management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService service;

    @Operation(summary = "Create a monthly budget", description = "Defines total and per-category spending limits for a given month")
    @ApiResponse(responseCode = "201", description = "Budget created successfully")
    @ApiResponse(responseCode = "400", description = "Budget already exists for the given month",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Validation error",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<BudgetResponseDTO> create(@RequestBody @Valid BudgetRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update a monthly budget", description = "Replaces total limit and category limits for the given month")
    @ApiResponse(responseCode = "200", description = "Budget updated successfully")
    @ApiResponse(responseCode = "404", description = "Budget or category not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Validation error",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping("/{year}/{month}")
    public ResponseEntity<BudgetResponseDTO> update(
            @Parameter(description = "Year", example = "2026") @PathVariable int year,
            @Parameter(description = "Month (1-12)", example = "5") @PathVariable int month,
            @RequestBody @Valid BudgetRequestDTO request) {
        return ResponseEntity.ok(service.update(year, month, request));
    }

    @Operation(summary = "Get budget overview",
            description = "Returns actual spending (PAID), pending obligations, projected total, recurring estimate, and health status")
    @ApiResponse(responseCode = "200", description = "Overview retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Budget not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{year}/{month}/overview")
    public ResponseEntity<BudgetOverviewDTO> getOverview(
            @Parameter(description = "Year", example = "2026") @PathVariable int year,
            @Parameter(description = "Month (1-12)", example = "5") @PathVariable int month) {
        return ResponseEntity.ok(service.getOverview(year, month));
    }
}
