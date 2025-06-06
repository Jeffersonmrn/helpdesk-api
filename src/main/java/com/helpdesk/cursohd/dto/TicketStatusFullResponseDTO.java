package com.helpdesk.cursohd.dto;

import java.util.Date;
import java.util.List;

import com.helpdesk.cursohd.enums.PriorityEnum;
import com.helpdesk.cursohd.enums.StatusEnum;

public record TicketStatusFullResponseDTO(String ticketId, UserDTO user, Date date, String title, Integer number,
		StatusEnum status, PriorityEnum priority, UserDTO assignedUser, String description, String image,
		List<?> changes) {
}
