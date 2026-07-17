package com.fruitlink.procurement.listener;

import com.fruitlink.procurement.event.StockLowEvent;
import com.fruitlink.procurement.service.ProcurementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockLowEventListener {

    private final ProcurementService procurementService;

    @Async
    @EventListener
    public void handleStockLowEvent(StockLowEvent event) {
        log.info("Received StockLowEvent for SKU: {}. Current stock: {}", event.getSkuId(), event.getCurrentStock());
        try {
            procurementService.evaluateAndGeneratePO(event.getSkuId());
        } catch (Exception e) {
            log.error("Failed to process auto-PO generation for SKU {}: {}", event.getSkuId(), e.getMessage(), e);
        }
    }
}
