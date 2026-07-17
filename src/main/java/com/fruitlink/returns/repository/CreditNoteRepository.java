package com.fruitlink.returns.repository;

import com.fruitlink.returns.entity.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID> {
    Optional<CreditNote> findByReturnRequestId(UUID returnId);
}
