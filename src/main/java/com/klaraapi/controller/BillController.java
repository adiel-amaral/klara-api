package com.klaraapi.controller;

import com.klaraapi.dto.*;
import com.klaraapi.enums.BillStatus;
import com.klaraapi.enums.Recurrence;
import com.klaraapi.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Bills", description = "Bill management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/bills")
public class BillController {

    private final BillService service;

    @Operation(summary = "Create a bill", description = "Creates a new bill and returns its data")
    @ApiResponse(responseCode = "201", description = "Bill created successfully")
    @ApiResponse(responseCode = "400", description = "Business rule violation",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Validation error",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<BillResponseDTO> create(@RequestBody @Valid BillRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update a bill", description = "Updates bill data excluding status")
    @ApiResponse(responseCode = "200", description = "Bill updated successfully")
    @ApiResponse(responseCode = "404", description = "Bill not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Validation error",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping("/{id}")
    public ResponseEntity<BillResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid BillUpdateRequestDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Update bill status", description = "Transitions bill status according to allowed rules")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status transition",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Bill not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Validation error",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping("/{id}/status")
    public ResponseEntity<BillResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid BillStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    @Operation(summary = "Delete a bill", description = "Permanently deletes a bill by id")
    @ApiResponse(responseCode = "204", description = "Bill deleted successfully")
    @ApiResponse(responseCode = "404", description = "Bill not found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List bills", description = "Returns a paginated list of bills")
    @ApiResponse(responseCode = "200", description = "Bills retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid sort field",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public ResponseEntity<Page<BillResponseDTO>> findAll(
            @Parameter(description = "Filter by status") @RequestParam(required = false) BillStatus status,
            @Parameter(description = "Filter by recurrence") @RequestParam(required = false) Recurrence recurrence,
            @Parameter(description = "Due date from (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @Parameter(description = "Due date to (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", schema = @Schema(allowableValues = {"dueDate", "amount", "createdAt", "name"}))
            @RequestParam(defaultValue = "dueDate") String sort,
            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String direction) {
        var filter = new BillFilter(status, recurrence, dueDateFrom, dueDateTo);
        return ResponseEntity.ok(service.findAll(filter, page, size, sort, direction));
    }
}
