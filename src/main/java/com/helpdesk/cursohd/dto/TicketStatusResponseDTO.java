package com.helpdesk.cursohd.dto;

import java.util.Date;

import com.helpdesk.cursohd.enums.StatusEnum;

public record TicketStatusResponseDTO(String ticketId, StatusEnum newStatus, Date dateChange, String changedBy) {
}
