package com.fruitlink.procurement.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StockLowEvent extends ApplicationEvent {
    private final String skuId;
    private final Double currentStock;

    public StockLowEvent(Object source, String skuId, Double currentStock) {
        super(source);
        this.skuId = skuId;
        this.currentStock = currentStock;
    }
}
