package com.fruitlink.jobs.job;

import com.fruitlink.orders.entity.Order;
import com.fruitlink.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsolidationJob implements Job {

    private final OrderRepository orderRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting OrderConsolidationJob: Sweeping confirmed orders...");

        try {
            List<Order> confirmedOrders = orderRepository.findByStatus("confirmed");
            
            if (confirmedOrders.isEmpty()) {
                log.info("No confirmed orders found for consolidation.");
                return;
            }

            log.info("Found {} confirmed orders to consolidate.", confirmedOrders.size());
            
            for (Order order : confirmedOrders) {
                // Change status to processing to indicate warehouse is handling it
                order.setStatus("processing");
                orderRepository.save(order);
                log.debug("Order {} status updated to processing.", order.getId());
            }

            log.info("OrderConsolidationJob completed successfully.");
        } catch (Exception e) {
            log.error("Error executing OrderConsolidationJob", e);
            throw new JobExecutionException(e);
        }
    }
}
