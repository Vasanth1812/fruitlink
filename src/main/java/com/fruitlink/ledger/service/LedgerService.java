package com.fruitlink.ledger.service;

import com.fruitlink.common.BusinessException;
import com.fruitlink.ledger.dto.LedgerDto.*;
import com.fruitlink.ledger.entity.*;
import com.fruitlink.ledger.repository.*;
import com.fruitlink.orders.entity.Order;
import com.fruitlink.orders.repository.OrderRepository;
import com.fruitlink.shops.entity.Shop;
import com.fruitlink.shops.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final InvoiceRepository invoiceRepository;
    private final TaxLineRepository taxLineRepository;
    private final PaymentRepository paymentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final com.fruitlink.audit.service.AuditService auditService;
    private final com.fruitlink.orders.repository.OrderItemRepository orderItemRepository;

    // ── Invoice ───────────────────────────────────────────

    @Transactional
    public InvoiceResponse generateInvoice(GenerateInvoiceRequest req, String actorPhone) {
        Order order = orderRepository.findById(UUID.fromString(req.getOrderId()))
                .orElseThrow(() -> new BusinessException("Order not found"));

        if (invoiceRepository.findByOrderId(order.getId()).isPresent())
            throw new BusinessException("Invoice already generated for this order");

        if (!"confirmed".equals(order.getStatus()) && !"delivered".equals(order.getStatus()))
            throw new BusinessException("Can only invoice confirmed or delivered orders");

        long subtotal = orderItemRepository.findByOrderId(order.getId())
                .stream().mapToLong(com.fruitlink.orders.entity.OrderItem::getTotalAmount).sum();

        // Auto-calc 18% GST (9% CGST, 9% SGST)
        long cgst = (long) (subtotal * 0.09);
        long sgst = (long) (subtotal * 0.09);
        long taxAmount = cgst + sgst;
        long total = subtotal + taxAmount;

        // Generate sequential invoice number: INV-YYYYMM-XXXX
        long count = invoiceRepository.count() + 1;
        String invoiceNumber = String.format("INV-%s-%04d",
                java.time.LocalDate.now().toString().substring(0, 7).replace("-", ""), count);

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setShop(order.getShop());
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(total);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setGstin(order.getShop().getGstin());
        invoice.setDueDate(req.getDueDate());
        invoice = invoiceRepository.save(invoice);

        // Save tax lines (for backward compatibility with tax line table if still used)
        TaxLine cgstLine = new TaxLine();
        cgstLine.setInvoice(invoice);
        cgstLine.setTaxType("CGST");
        cgstLine.setRate(new java.math.BigDecimal("9.00"));
        cgstLine.setAmount(cgst);
        taxLineRepository.save(cgstLine);

        TaxLine sgstLine = new TaxLine();
        sgstLine.setInvoice(invoice);
        sgstLine.setTaxType("SGST");
        sgstLine.setRate(new java.math.BigDecimal("9.00"));
        sgstLine.setAmount(sgst);
        taxLineRepository.save(sgstLine);

        // Post ledger entry (debit shop)
        postLedgerEntry(order.getShop(), order, "invoice", total,
                "Invoice " + invoiceNumber);

        auditService.logAction(actorPhone, "GENERATE_INVOICE", "LEDGER", "{}", "{\"invoiceId\":\"" + invoice.getId() + "\", \"total\":" + total + "}");

        return toInvoiceResponse(invoice);
    }

    public InvoiceResponse getInvoice(String id) {
        return toInvoiceResponse(findInvoice(id));
    }

    public List<InvoiceResponse> getByShop(String shopId) {
        return invoiceRepository.findByShopId(UUID.fromString(shopId))
                .stream().map(this::toInvoiceResponse).toList();
    }

    public List<InvoiceResponse> getByStatus(String status) {
        return invoiceRepository.findByStatus(status)
                .stream().map(this::toInvoiceResponse).toList();
    }

    // ── Payment ───────────────────────────────────────────

    @Transactional
    public PaymentResponse recordPayment(RecordPaymentRequest req) {
        Invoice invoice = findInvoice(req.getInvoiceId());

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(req.getAmount());
        payment.setMethod(req.getMethod());
        payment.setReference(req.getReference());
        payment.setPaidAt(Instant.now());
        payment = paymentRepository.save(payment);

        // Update invoice status
        long paid = paymentRepository.sumAmountByInvoiceId(invoice.getId());
        if (paid >= invoice.getTotal()) invoice.setStatus("paid");
        else invoice.setStatus("partial");
        invoiceRepository.save(invoice);

        // Post ledger entry (credit shop — payment received)
        postLedgerEntry(invoice.getShop(), invoice.getOrder(), "payment",
                -req.getAmount(), "Payment via " + req.getMethod());

        auditService.logAction("SYSTEM", "RECORD_PAYMENT", "LEDGER", "{}", "{\"invoiceId\":\"" + invoice.getId() + "\", \"amount\":" + req.getAmount() + "}");

        PaymentResponse r = new PaymentResponse();
        r.setId(payment.getId().toString());
        r.setInvoiceId(invoice.getId().toString());
        r.setAmount(payment.getAmount());
        r.setMethod(payment.getMethod());
        r.setReference(payment.getReference());
        r.setPaidAt(payment.getPaidAt());
        return r;
    }

    public List<PaymentResponse> getPaymentsByInvoice(String invoiceId) {
        return paymentRepository.findByInvoiceId(UUID.fromString(invoiceId))
                .stream().map(p -> {
                    PaymentResponse r = new PaymentResponse();
                    r.setId(p.getId().toString());
                    r.setInvoiceId(invoiceId);
                    r.setAmount(p.getAmount());
                    r.setMethod(p.getMethod());
                    r.setReference(p.getReference());
                    r.setPaidAt(p.getPaidAt());
                    return r;
                }).toList();
    }

    // ── Ledger ────────────────────────────────────────────

    public ShopLedgerResponse getShopLedger(String shopId) {
        Shop shop = shopRepository.findById(UUID.fromString(shopId))
                .orElseThrow(() -> new BusinessException("Shop not found"));

        List<LedgerEntry> entries = ledgerEntryRepository
                .findByShopIdOrderByCreatedAtDesc(UUID.fromString(shopId));

        ShopLedgerResponse r = new ShopLedgerResponse();
        r.setShopId(shopId);
        r.setShopName(shop.getName());
        r.setCurrentBalance(entries.isEmpty() ? 0L : entries.get(0).getBalanceAfter());
        r.setEntries(entries.stream().map(e -> {
            LedgerEntryResponse lr = new LedgerEntryResponse();
            lr.setId(e.getId().toString());
            lr.setType(e.getType());
            lr.setAmount(e.getAmount());
            lr.setBalanceAfter(e.getBalanceAfter());
            lr.setNotes(e.getNotes());
            lr.setCreatedAt(e.getCreatedAt());
            return lr;
        }).toList());
        return r;
    }

    // ── Helpers ───────────────────────────────────────────

    private Invoice findInvoice(String id) {
        return invoiceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException("Invoice not found"));
    }

    private void postLedgerEntry(Shop shop, Order order, String type, long amount, String notes) {
        List<LedgerEntry> recent = ledgerEntryRepository
                .findByShopIdOrderByCreatedAtDesc(shop.getId());
        long prevBalance = recent.isEmpty() ? 0L : recent.get(0).getBalanceAfter();

        LedgerEntry entry = new LedgerEntry();
        entry.setShop(shop);
        entry.setOrder(order);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setBalanceAfter(prevBalance + amount);
        entry.setNotes(notes);
        ledgerEntryRepository.save(entry);
    }

    private InvoiceResponse toInvoiceResponse(Invoice inv) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(inv.getId().toString());
        r.setInvoiceNumber(inv.getInvoiceNumber());
        r.setOrderId(inv.getOrder().getId().toString());
        r.setShopId(inv.getShop().getId().toString());
        r.setShopName(inv.getShop().getName());
        r.setSubtotal(inv.getSubtotal());
        r.setTaxAmount(inv.getTaxAmount());
        r.setTotal(inv.getTotal());
        r.setStatus(inv.getStatus());
        r.setDueDate(inv.getDueDate());
        r.setCreatedAt(inv.getCreatedAt());
        long paid = paymentRepository.sumAmountByInvoiceId(inv.getId());
        r.setAmountPaid(paid);
        r.setAmountDue(inv.getTotal() - paid);
        r.setTaxLines(taxLineRepository.findByInvoiceId(inv.getId()).stream().map(tl -> {
            TaxLineResponse tlr = new TaxLineResponse();
            tlr.setId(tl.getId().toString());
            tlr.setTaxType(tl.getTaxType());
            tlr.setRate(tl.getRate());
            tlr.setAmount(tl.getAmount());
            return tlr;
        }).toList());
        return r;
    }
}
