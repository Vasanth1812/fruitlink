package com.fruitlink.crates.service;

import com.fruitlink.crates.dto.CrateDto.*;
import com.fruitlink.crates.entity.CrateBalance;
import com.fruitlink.crates.entity.CrateTransaction;
import com.fruitlink.crates.repository.CrateBalanceRepository;
import com.fruitlink.crates.repository.CrateTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrateService {

    private final CrateTransactionRepository transactionRepository;
    private final CrateBalanceRepository balanceRepository;

    @Transactional
    public CrateTransactionResponse recordTransaction(RecordTransactionRequest req) {
        UUID partyId = UUID.fromString(req.getPartyId());
        
        CrateTransaction tx = new CrateTransaction();
        tx.setType(req.getType());
        tx.setPartyId(partyId);
        tx.setPartyType(req.getPartyType());
        tx.setQuantity(req.getQuantity());
        tx.setReferenceId(req.getReferenceId());
        
        tx = transactionRepository.save(tx);
        
        CrateBalance balance = balanceRepository.findByPartyIdAndPartyType(partyId, req.getPartyType())
            .orElseGet(() -> {
                CrateBalance newBal = new CrateBalance();
                newBal.setPartyId(partyId);
                newBal.setPartyType(req.getPartyType());
                newBal.setCurrentBalance(0);
                return newBal;
            });
            
        if ("issue".equalsIgnoreCase(req.getType())) {
            balance.setCurrentBalance(balance.getCurrentBalance() + req.getQuantity());
        } else if ("return".equalsIgnoreCase(req.getType())) {
            balance.setCurrentBalance(balance.getCurrentBalance() - req.getQuantity());
        }
        
        balanceRepository.save(balance);
        return toTransactionResponse(tx);
    }

    public List<CrateTransactionResponse> getTransactions(String partyId) {
        return transactionRepository.findByPartyIdOrderByDateDesc(UUID.fromString(partyId))
                .stream().map(this::toTransactionResponse).collect(Collectors.toList());
    }

    public CrateBalanceResponse getBalance(String partyId, String partyType) {
        return balanceRepository.findByPartyIdAndPartyType(UUID.fromString(partyId), partyType)
                .map(this::toBalanceResponse)
                .orElseGet(() -> {
                    CrateBalanceResponse empty = new CrateBalanceResponse();
                    empty.setPartyId(partyId);
                    empty.setPartyType(partyType);
                    empty.setCurrentBalance(0);
                    return empty;
                });
    }

    private CrateTransactionResponse toTransactionResponse(CrateTransaction t) {
        CrateTransactionResponse r = new CrateTransactionResponse();
        r.setId(t.getId().toString());
        r.setType(t.getType());
        r.setPartyId(t.getPartyId().toString());
        r.setPartyType(t.getPartyType());
        r.setQuantity(t.getQuantity());
        r.setReferenceId(t.getReferenceId());
        r.setDate(t.getDate());
        return r;
    }
    
    private CrateBalanceResponse toBalanceResponse(CrateBalance b) {
        CrateBalanceResponse r = new CrateBalanceResponse();
        r.setPartyId(b.getPartyId().toString());
        r.setPartyType(b.getPartyType());
        r.setCurrentBalance(b.getCurrentBalance());
        return r;
    }
}
