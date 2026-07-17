package com.fruitlink.helpdesk.service;

import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.common.BusinessException;
import com.fruitlink.helpdesk.dto.HelpdeskDto.*;
import com.fruitlink.helpdesk.entity.Ticket;
import com.fruitlink.helpdesk.entity.TicketMessage;
import com.fruitlink.helpdesk.repository.TicketMessageRepository;
import com.fruitlink.helpdesk.repository.TicketRepository;
import com.fruitlink.shops.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HelpdeskService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest req, String phone) {
        var user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setRaisedBy(user);
        ticket.setSubject(req.getSubject());
        ticket.setDescription(req.getDescription());
        ticket.setPriority(req.getPriority() != null ? req.getPriority() : "medium");

        if (req.getShopId() != null)
            shopRepository.findById(UUID.fromString(req.getShopId()))
                    .ifPresent(ticket::setShop);

        return toResponse(ticketRepository.save(ticket));
    }

    public List<TicketResponse> getAll(String status, String priority) {
        if (status != null) return ticketRepository.findByStatus(status).stream().map(this::toResponse).toList();
        if (priority != null) return ticketRepository.findByPriority(priority).stream().map(this::toResponse).toList();
        return ticketRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<TicketResponse> getMyTickets(String phone) {
        var user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found"));
        return ticketRepository.findByRaisedById(user.getId()).stream().map(this::toResponse).toList();
    }

    public TicketResponse getTicket(String id) {
        return toResponse(find(id));
    }

    @Transactional
    public TicketResponse updateTicket(String id, UpdateTicketRequest req) {
        Ticket ticket = find(id);
        if (req.getStatus() != null) ticket.setStatus(req.getStatus());
        if (req.getPriority() != null) ticket.setPriority(req.getPriority());
        if (req.getAssignedToId() != null)
            userRepository.findById(UUID.fromString(req.getAssignedToId()))
                    .ifPresent(ticket::setAssignedTo);
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public MessageResponse addMessage(String ticketId, AddMessageRequest req, String phone) {
        Ticket ticket = find(ticketId);
        var sender = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found"));

        TicketMessage msg = new TicketMessage();
        msg.setTicket(ticket);
        msg.setSender(sender);
        msg.setMessage(req.getMessage());
        msg = messageRepository.save(msg);

        // Auto set to in_progress when agent replies
        if ("open".equals(ticket.getStatus())) {
            ticket.setStatus("in_progress");
            ticketRepository.save(ticket);
        }

        return toMessageResponse(msg);
    }

    // ── Helpers ────────────────────────────────────────────

    private Ticket find(String id) {
        return ticketRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException("Ticket not found"));
    }

    private TicketResponse toResponse(Ticket t) {
        TicketResponse r = new TicketResponse();
        r.setId(t.getId().toString());
        r.setSubject(t.getSubject());
        r.setDescription(t.getDescription());
        r.setStatus(t.getStatus());
        r.setPriority(t.getPriority());
        r.setRaisedById(t.getRaisedBy().getId().toString());
        r.setRaisedByName(t.getRaisedBy().getFullName());
        r.setCreatedAt(t.getCreatedAt());
        r.setUpdatedAt(t.getUpdatedAt());
        if (t.getShop() != null) {
            r.setShopId(t.getShop().getId().toString());
            r.setShopName(t.getShop().getName());
        }
        if (t.getAssignedTo() != null) {
            r.setAssignedToId(t.getAssignedTo().getId().toString());
            r.setAssignedToName(t.getAssignedTo().getFullName());
        }
        r.setMessages(messageRepository.findByTicketIdOrderByCreatedAtAsc(t.getId())
                .stream().map(this::toMessageResponse).toList());
        return r;
    }

    private MessageResponse toMessageResponse(TicketMessage m) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId().toString());
        r.setSenderId(m.getSender().getId().toString());
        r.setSenderName(m.getSender().getFullName());
        r.setMessage(m.getMessage());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}
