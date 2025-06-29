package com.helpdesk.cursohd.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helpdesk.cursohd.security.entities.ChangeStatus;

public interface ChangeStatusRepository extends MongoRepository<ChangeStatus, String> {

	Iterable<ChangeStatus> findByTicketIdOrderByDateChangeStatusDesc(String ticketId);
}
