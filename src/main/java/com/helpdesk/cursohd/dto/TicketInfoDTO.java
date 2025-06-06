package com.helpdesk.cursohd.dto;

import java.util.Date;
import java.util.List;

import com.helpdesk.cursohd.security.entities.ChangeStatus;
import com.helpdesk.cursohd.security.entities.Ticket;

public class TicketInfoDTO {
	private Date date;
	private String title;
	private Integer number;
	private String status;
	private String priority;
	private UserPublicDTO assignedUser;
	private String description;
	private String image;
	private List<ChangeStatus> changes;

	public TicketInfoDTO(Ticket ticket) {
		this.date = ticket.getDate();
		this.title = ticket.getTitle();
		this.number = ticket.getNumber();
		this.status = ticket.getStatus().name();
		this.priority = ticket.getPriority().name();
		this.assignedUser = ticket.getAssignedUser() != null ? new UserPublicDTO(ticket.getAssignedUser()) : null;
		this.description = ticket.getDescription();
		this.image = ticket.getImage();
		this.changes = ticket.getChanges();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public UserPublicDTO getAssignedUser() {
		return assignedUser;
	}

	public void setAssignedUser(UserPublicDTO assignedUser) {
		this.assignedUser = assignedUser;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<ChangeStatus> getChanges() {
		return changes;
	}

	public void setChanges(List<ChangeStatus> changes) {
		this.changes = changes;
	}
}
