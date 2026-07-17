package com.fruitlink.helpdesk.repository;

import com.fruitlink.helpdesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByStatus(String status);
    List<Ticket> findByPriority(String priority);

    @Query("SELECT t FROM Ticket t WHERE t.raisedBy.id = :userId")
    List<Ticket> findByRaisedById(UUID userId);

    @Query("SELECT t FROM Ticket t WHERE t.assignedTo.id = :userId")
    List<Ticket> findByAssignedToId(UUID userId);
}
