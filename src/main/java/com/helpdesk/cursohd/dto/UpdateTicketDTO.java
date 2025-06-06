package com.helpdesk.cursohd.dto;

import com.helpdesk.cursohd.enums.PriorityEnum;

public record UpdateTicketDTO(String ticketId, String title, PriorityEnum priority, String description, String image) {
}
