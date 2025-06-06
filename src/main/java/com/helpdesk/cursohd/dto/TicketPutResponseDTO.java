package com.helpdesk.cursohd.dto;

import java.util.Date;

import com.helpdesk.cursohd.enums.PriorityEnum;
import com.helpdesk.cursohd.enums.StatusEnum;

public record TicketPutResponseDTO(String ticketId, UserDTO user, Date date, String title, Integer number,
		StatusEnum status, PriorityEnum priority, UserDTO assignedUser, String description, String image,
		Object changes) {
}
