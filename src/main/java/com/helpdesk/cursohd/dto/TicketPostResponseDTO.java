package com.helpdesk.cursohd.dto;

import java.util.List;

import com.helpdesk.cursohd.security.entities.Ticket;
import com.helpdesk.cursohd.security.entities.User;

public class TicketPostResponseDTO {

	private String ticketId;
	private List<UserPublicDTO> user;
	private TicketInfoDTO ticket;

	public TicketPostResponseDTO(Ticket ticket, User user) {
		this.ticketId = ticket.getId();
		this.user = List.of(new UserPublicDTO(user));
		this.ticket = new TicketInfoDTO(ticket);
	}

	public String getTicketId() {
		return ticketId;
	}

	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}

	public List<UserPublicDTO> getUser() {
		return user;
	}

	public void setUser(List<UserPublicDTO> user) {
		this.user = user;
	}

	public TicketInfoDTO getTicket() {
		return ticket;
	}

	public void setTicket(TicketInfoDTO ticket) {
		this.ticket = ticket;
	}
}
