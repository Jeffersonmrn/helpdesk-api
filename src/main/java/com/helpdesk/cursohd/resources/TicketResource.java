package com.helpdesk.cursohd.resources;

import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.cursohd.dto.Summary;
import com.helpdesk.cursohd.dto.TicketPostResponseDTO;
import com.helpdesk.cursohd.dto.TicketPutResponseDTO;
import com.helpdesk.cursohd.dto.TicketStatusFullResponseDTO;
import com.helpdesk.cursohd.dto.UpdateTicketDTO;
import com.helpdesk.cursohd.dto.UserDTO;
import com.helpdesk.cursohd.enums.ProfileEnum;
import com.helpdesk.cursohd.enums.StatusEnum;
import com.helpdesk.cursohd.repositories.ChangeStatusRepository;
import com.helpdesk.cursohd.repositories.TicketRepository;
import com.helpdesk.cursohd.response.Response;
import com.helpdesk.cursohd.security.entities.ChangeStatus;
import com.helpdesk.cursohd.security.entities.Ticket;
import com.helpdesk.cursohd.security.entities.User;
import com.helpdesk.cursohd.security.service.TokenService;
import com.helpdesk.cursohd.service.TicketService;
import com.helpdesk.cursohd.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins = "*")
public class TicketResource {

	@Autowired
	private TicketService ticketService;

	@Autowired
	protected TokenService tokenService;

	@Autowired
	private UserService userService;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private ChangeStatusRepository changeStatusRepository;

	@PostMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<TicketPostResponseDTO>> create(HttpServletRequest request,
			@RequestBody Ticket ticket, BindingResult result) {

		Response<TicketPostResponseDTO> response = new Response<>();

		try {

			validateCreateTicket(ticket, result);

			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}

			User user = userFromRequest(request);

			if (user == null) {
				response.getErrors().add("Usuário autenticado não encontrado.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			ticket.setUser(user);
			ticket.setStatus(StatusEnum.getStatus("New"));
			ticket.setDate(new Date());
			ticket.setNumber(generateNumber());

			Ticket ticketPersisted = ticketService.createOrUpdate(ticket);

			TicketPostResponseDTO dto = new TicketPostResponseDTO(ticketPersisted, user);
			response.setData(dto);

		} catch (Exception e) {
			response.getErrors().add("Erro ao criar ticket: " + e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}

		return ResponseEntity.ok(response);
	}

	private void validateCreateTicket(Ticket ticket, BindingResult result) {
		if (ticket.getTitle() == null || ticket.getTitle().trim().isEmpty()) {
			result.addError(new ObjectError("Ticket", "Title is required"));
		}
	}

	private User userFromRequest(HttpServletRequest request) {
		String token = request.getHeader("Authorization");

		if (token == null || !token.startsWith("Bearer ")) {
			return null;
		}

		token = token.replace("Bearer ", "").trim();
		String email = tokenService.getEmailFromToken(token);
		return userService.findByEmail(email);
	}

	private Integer generateNumber() {
		return new Random().nextInt(9999);
	}

	@PutMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<TicketPutResponseDTO>> updateTicket(@RequestBody UpdateTicketDTO dto) {
		Optional<Ticket> optionalTicket = ticketRepository.findById(dto.ticketId());

		Response<TicketPutResponseDTO> response = new Response<>();

		if (optionalTicket.isEmpty()) {
			response.getErrors().add("Ticket não encontrado.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Ticket ticket = optionalTicket.get();

		ticket.setTitle(dto.title());
		ticket.setPriority(dto.priority());
		ticket.setDescription(dto.description());
		ticket.setImage(dto.image());

		Ticket updated = ticketRepository.save(ticket);

		UserDTO userDTO = new UserDTO(updated.getUser().getId(), updated.getUser().getEmail(),
				updated.getUser().getPassword(), updated.getUser().getProfile());

		UserDTO assignedUserDTO = null;
		if (updated.getAssignedUser() != null) {
			assignedUserDTO = new UserDTO(updated.getAssignedUser().getId(), updated.getAssignedUser().getEmail(),
					updated.getAssignedUser().getPassword(), updated.getAssignedUser().getProfile());
		}

		TicketPutResponseDTO dtoResponse = new TicketPutResponseDTO(updated.getId(), userDTO, updated.getDate(),
				updated.getTitle(), updated.getNumber(), updated.getStatus(), updated.getPriority(), assignedUserDTO,
				updated.getDescription(), updated.getImage(), updated.getChanges());

		response.setData(dtoResponse);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
	public ResponseEntity<Response<TicketPutResponseDTO>> getTicketById(@PathVariable String id) {
		Response<TicketPutResponseDTO> response = new Response<>();

		Optional<Ticket> optionalTicket = ticketRepository.findById(id);

		if (optionalTicket.isEmpty()) {
			response.getErrors().add("Ticket não encontrado.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Ticket ticket = optionalTicket.get();

		User user = ticket.getUser();
		UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getPassword(), user.getProfile());

		User assignedUser = ticket.getAssignedUser();
		UserDTO assignedUserDTO = (assignedUser != null)
				? new UserDTO(assignedUser.getId(), assignedUser.getEmail(), assignedUser.getPassword(),
						assignedUser.getProfile())
				: null;

		TicketPutResponseDTO dtoResponse = new TicketPutResponseDTO(ticket.getId(), userDTO, ticket.getDate(),
				ticket.getTitle(), ticket.getNumber(), ticket.getStatus(), ticket.getPriority(), assignedUserDTO,
				ticket.getDescription(), ticket.getImage(), ticket.getChanges());

		response.setData(dtoResponse);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
	public ResponseEntity<Response<String>> deleteTicket(@PathVariable String id, Authentication authentication) {
		Response<String> response = new Response<>();

		Optional<Ticket> optionalTicket = ticketRepository.findById(id);
		if (optionalTicket.isEmpty()) {
			response.getErrors().add("Ticket não encontrado.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Ticket ticket = optionalTicket.get();
		User authenticatedUser = (User) authentication.getPrincipal();

		boolean isAdmin = authenticatedUser.getProfile().name().equals("ROLE_ADMIN");
		boolean isOwner = ticket.getUser().getId().equals(authenticatedUser.getId());

		if (!isAdmin && !isOwner) {
			response.getErrors().add("Você não tem permissão para deletar este ticket.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		ticketRepository.deleteById(id);
		response.setData("Ticket deletado com sucesso.");
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = "/{page}/{count}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
	public ResponseEntity<Response<Page<TicketPutResponseDTO>>> findAllTickets(@PathVariable int page,
			@PathVariable int count) {

		Page<Ticket> tickets = ticketService.listTicket(page, count);

		Page<TicketPutResponseDTO> dtoPage = tickets.map(ticket -> {
			User user = ticket.getUser();
			UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getPassword(), user.getProfile());

			return new TicketPutResponseDTO(ticket.getId(), userDTO, ticket.getDate(), ticket.getTitle(),
					ticket.getNumber(), ticket.getStatus(), ticket.getPriority(), null,

					ticket.getDescription(), ticket.getImage(), ticket.getChanges());
		});

		Response<Page<TicketPutResponseDTO>> response = new Response<>();
		response.setData(dtoPage);
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = "{page}/{count}/{number}/{title}/{status}/{priority}/{assigned}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findByParams(HttpServletRequest request, @PathVariable int page,
			@PathVariable int count, @PathVariable Integer number, @PathVariable String title,
			@PathVariable String status, @PathVariable String priority, @PathVariable boolean assigned) {

		title = title.equals("uninformed") ? "" : title;
		status = status.equals("uninformed") ? "" : status;
		priority = priority.equals("uninformed") ? "" : priority;

		Response<Page<Ticket>> response = new Response<Page<Ticket>>();
		Page<Ticket> tickets = null;
		if (number > 0) {
			tickets = ticketService.findByNumber(page, count, number);
		} else {
			User userRequest = userFromRequest(request);
			if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
				if (assigned) {
					tickets = ticketService.findByParametersAndAssignedUser(page, count, title, status, priority,
							userRequest.getId());
				} else {
					tickets = ticketService.findByParameters(page, count, title, status, priority);
				}
			} else if (userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
				tickets = ticketService.findByParametersAndCurrentUser(page, count, title, status, priority,
						userRequest.getId());
			}
		}
		response.setData(tickets);
		return ResponseEntity.ok(response);
	}

	@PutMapping(value = "/{id}/{status}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
	public ResponseEntity<Response<TicketStatusFullResponseDTO>> updateTicketStatus(@PathVariable String id,
			@PathVariable String status, Authentication authentication) {

		Response<TicketStatusFullResponseDTO> response = new Response<>();

		Optional<Ticket> optionalTicket = ticketRepository.findById(id);
		if (optionalTicket.isEmpty()) {
			response.getErrors().add("Ticket não encontrado.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Ticket ticket = optionalTicket.get();

		StatusEnum newStatus;
		try {
			newStatus = StatusEnum.valueOf(status);
		} catch (IllegalArgumentException e) {
			response.getErrors().add("Status inválido: " + status);
			return ResponseEntity.badRequest().body(response);
		}

		ticket.setStatus(newStatus);
		ticketRepository.save(ticket);

		User user = userService.findByEmail(authentication.getName());

		ChangeStatus change = new ChangeStatus();
		change.setUserChange(user);
		change.setDateChangeStatus(new java.sql.Date(System.currentTimeMillis()));
		change.setStatus(newStatus);
		change.setTicket(ticket);
		changeStatusRepository.save(change);

		UserDTO userDTO = UserDTO.fromEntity(ticket.getUser());
		UserDTO assignedDTO = ticket.getAssignedUser() != null ? UserDTO.fromEntity(ticket.getAssignedUser()) : null;

		TicketStatusFullResponseDTO dto = new TicketStatusFullResponseDTO(ticket.getId(), userDTO, ticket.getDate(),
				ticket.getTitle(), ticket.getNumber(), ticket.getStatus(), ticket.getPriority(), assignedDTO,
				ticket.getDescription(), ticket.getImage(), null);

		response.setData(dto);
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = "/summary")
	public ResponseEntity<Response<Summary>> findChart() {
		Response<Summary> response = new Response<Summary>();
		Summary chart = new Summary();
		int amountNew = 0;
		int amountResolved = 0;
		int amountApproved = 0;
		int amountDisapproved = 0;
		int amountAssigned = 0;
		int amountClosed = 0;
		Iterable<Ticket> tickets = ticketService.findAll();
		if (tickets != null) {
			for (Iterator<Ticket> iterator = tickets.iterator(); iterator.hasNext();) {
				Ticket ticket = iterator.next();
				if (ticket.getStatus().equals(StatusEnum.New)) {
					amountNew++;
				}
				if (ticket.getStatus().equals(StatusEnum.Resolved)) {
					amountResolved++;
				}
				if (ticket.getStatus().equals(StatusEnum.Approved)) {
					amountApproved++;
				}
				if (ticket.getStatus().equals(StatusEnum.Disapproved)) {
					amountDisapproved++;
				}
				if (ticket.getStatus().equals(StatusEnum.Assigned)) {
					amountAssigned++;
				}
				if (ticket.getStatus().equals(StatusEnum.Closed)) {
					amountClosed++;
				}
			}
		}
		chart.setAmountNew(amountNew);
		chart.setAmountResolved(amountResolved);
		chart.setAmountApproved(amountApproved);
		chart.setAmountDisapproved(amountDisapproved);
		chart.setAmountAssigned(amountAssigned);
		chart.setAmountClosed(amountClosed);
		response.setData(chart);
		return ResponseEntity.ok(response);
	}
}
