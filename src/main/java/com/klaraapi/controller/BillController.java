package com.klaraapi.controller;

import com.klaraapi.dto.BillRequestDTO;
import com.klaraapi.dto.BillResponseDTO;
import com.klaraapi.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService service;

    @PostMapping
    public ResponseEntity<BillResponseDTO> create(@RequestBody @Valid BillRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
}
