package com.klaraapi.integration.waha.dto;

public record WahaSendTextRequestDto(
        String session,
        String chatId,
        String text
) {
}
