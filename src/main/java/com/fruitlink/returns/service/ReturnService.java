package com.fruitlink.returns.service;

import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.common.BusinessException;
import com.fruitlink.inventory.repository.SkuRepository;
import com.fruitlink.ledger.entity.Invoice;
import com.fruitlink.ledger.entity.LedgerEntry;
import com.fruitlink.ledger.repository.InvoiceRepository;
import com.fruitlink.ledger.repository.LedgerEntryRepository;
import com.fruitlink.orders.repository.OrderRepository;
import com.fruitlink.returns.dto.ReturnDto.*;
import com.fruitlink.returns.entity.CreditNote;
import com.fruitlink.returns.entity.ReturnItem;
import com.fruitlink.returns.entity.ReturnRequest;
import com.fruitlink.returns.repository.CreditNoteRepository;
import com.fruitlink.returns.repository.ReturnItemRepository;
import com.fruitlink.returns.repository.ReturnRequestRepository;
import com.fruitlink.shops.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRequestRepository returnRepo;
    private final ReturnItemRepository itemRepo;
    private final CreditNoteRepository creditNoteRepo;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final SkuRepository skuRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final com.fruitlink.audit.service.AuditService auditService;

    // ── Create Return Request ──────────────────────────────

    @Transactional
    public ReturnResponse createReturn(CreateReturnRequest req, String requesterPhone) {
        var order = orderRepository.findById(UUID.fromString(req.getOrderId()))
                .orElseThrow(() -> new BusinessException("Order not found"));

        ReturnRequest ret = new ReturnRequest();
        ret.setOrder(order);
        ret.setShop(order.getShop());
        ret.setReason(req.getReason());
        userRepository.findByPhone(requesterPhone).ifPresent(ret::setRequestedBy);
        ret = returnRepo.save(ret);

        for (ReturnItemRequest itemReq : req.getItems()) {
            var sku = skuRepository.findById(UUID.fromString(itemReq.getSkuId()))
                    .orElseThrow(() -> new BusinessException("SKU not found: " + itemReq.getSkuId()));
            ReturnItem item = new ReturnItem();
            item.setReturnRequest(ret);
            item.setSku(sku);
            item.setQuantity(itemReq.getQuantity());
            item.setReason(itemReq.getReason());
            itemRepo.save(item);
        }

        return toResponse(ret);
    }

    // ── Review (Approve / Reject) ──────────────────────────

    @Transactional
    public ReturnResponse reviewReturn(String id, ReviewReturnRequest req) {
        ReturnRequest ret = find(id);
        if (!"pending".equals(ret.getStatus()))
            throw new BusinessException("Only pending returns can be reviewed");
        ret.setStatus(req.getStatus());
        return toResponse(returnRepo.save(ret));
    }

    // ── Issue Credit Note ──────────────────────────────────

    @Transactional
    public CreditNoteResponse issueCreditNote(IssueCreditNoteRequest req) {
        ReturnRequest ret = find(req.getReturnId());

        if (!"approved".equals(ret.getStatus()))
            throw new BusinessException("Return must be approved before issuing credit note");

        if (creditNoteRepo.findByReturnRequestId(ret.getId()).isPresent())
            throw new BusinessException("Credit note already issued for this return");

        Invoice invoice = null;
        if (req.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(UUID.fromString(req.getInvoiceId()))
                    .orElseThrow(() -> new BusinessException("Invoice not found"));
        }

        CreditNote cn = new CreditNote();
        cn.setReturnRequest(ret);
        cn.setInvoice(invoice);
        cn.setAmount(req.getAmount());
        cn.setStatus("issued");
        cn = creditNoteRepo.save(cn);

        // Post ledger credit entry
        List<LedgerEntry> recent = ledgerEntryRepository
                .findByShopIdOrderByCreatedAtDesc(ret.getShop().getId());
        long prevBalance = recent.isEmpty() ? 0L : recent.get(0).getBalanceAfter();
        LedgerEntry entry = new LedgerEntry();
        entry.setShop(ret.getShop());
        entry.setOrder(ret.getOrder());
        entry.setType("credit_note");
        entry.setAmount(-req.getAmount());
        entry.setBalanceAfter(prevBalance - req.getAmount());
        entry.setNotes("Credit note for return " + ret.getId());
        ledgerEntryRepository.save(entry);

        // Complete return
        ret.setStatus("completed");
        returnRepo.save(ret);

        auditService.logAction("SYSTEM", "ISSUE_CREDIT_NOTE", "LEDGER", "{}", "{\"creditNoteId\":\"" + cn.getId() + "\", \"amount\":" + req.getAmount() + "}");

        return toCreditNoteResponse(cn);
    }

    // ── Queries ────────────────────────────────────────────

    public List<ReturnResponse> getAll(String status, String shopId) {
        List<ReturnRequest> list;
        if (status != null) list = returnRepo.findByStatus(status);
        else if (shopId != null) list = returnRepo.findByShopId(UUID.fromString(shopId));
        else list = returnRepo.findAll();
        return list.stream().map(this::toResponse).toList();
    }

    public ReturnResponse getReturn(String id) {
        return toResponse(find(id));
    }

    // ── Helpers ────────────────────────────────────────────

    private ReturnRequest find(String id) {
        return returnRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException("Return request not found"));
    }

    private ReturnResponse toResponse(ReturnRequest r) {
        ReturnResponse res = new ReturnResponse();
        res.setId(r.getId().toString());
        res.setOrderId(r.getOrder().getId().toString());
        res.setShopId(r.getShop().getId().toString());
        res.setShopName(r.getShop().getName());
        res.setReason(r.getReason());
        res.setStatus(r.getStatus());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        if (r.getRequestedBy() != null) res.setRequestedById(r.getRequestedBy().getId().toString());

        res.setItems(itemRepo.findByReturnRequestId(r.getId()).stream().map(item -> {
            ReturnItemResponse ir = new ReturnItemResponse();
            ir.setId(item.getId().toString());
            ir.setSkuId(item.getSku().getId().toString());
            ir.setSkuName(item.getSku().getName());
            ir.setQuantity(item.getQuantity());
            ir.setReason(item.getReason());
            return ir;
        }).toList());

        creditNoteRepo.findByReturnRequestId(r.getId())
                .ifPresent(cn -> res.setCreditNote(toCreditNoteResponse(cn)));
        return res;
    }

    private CreditNoteResponse toCreditNoteResponse(CreditNote cn) {
        CreditNoteResponse r = new CreditNoteResponse();
        r.setId(cn.getId().toString());
        r.setReturnId(cn.getReturnRequest().getId().toString());
        if (cn.getInvoice() != null) r.setInvoiceId(cn.getInvoice().getId().toString());
        r.setAmount(cn.getAmount());
        r.setStatus(cn.getStatus());
        r.setCreatedAt(cn.getCreatedAt());
        return r;
    }
}
