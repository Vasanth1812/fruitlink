package com.fruitlink.audit.service;

import com.fruitlink.audit.entity.AuditLog;
import com.fruitlink.audit.repository.AuditLogRepository;
import com.fruitlink.auth.entity.User;
import com.fruitlink.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // Propagation.MANDATORY ensures it runs in the existing transaction of the caller
    // So if the caller's transaction rolls back, the audit log rolls back too.
    @Transactional(propagation = Propagation.MANDATORY)
    public void logAction(String actorPhone, String action, String module, String beforeState, String afterState) {
        AuditLog log = new AuditLog();
        if (actorPhone != null) {
            userRepository.findByPhone(actorPhone).ifPresent(log::setActor);
        }
        log.setAction(action);
        log.setModule(module);
        log.setBeforeState(beforeState);
        log.setAfterState(afterState);
        auditLogRepository.save(log);
    }
}
