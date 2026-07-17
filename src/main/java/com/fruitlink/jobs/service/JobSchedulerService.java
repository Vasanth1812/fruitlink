package com.fruitlink.jobs.service;

import com.fruitlink.jobs.job.AgingReceivablesSweepJob;
import com.fruitlink.jobs.job.OrderConsolidationJob;
import com.fruitlink.jobs.job.RouteOptimizationJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.quartz.enabled", havingValue = "true")
public class JobSchedulerService {

    private final Scheduler scheduler;

    @PostConstruct
    public void scheduleJobs() {
        try {
            scheduleOrderConsolidationJob();
            scheduleRouteOptimizationJob();
            scheduleAgingReceivablesSweepJob();
        } catch (SchedulerException e) {
            log.error("Failed to schedule jobs", e);
        }
    }

    private void scheduleOrderConsolidationJob() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(OrderConsolidationJob.class)
                .withIdentity("OrderConsolidationJob", "FruitLinkJobs")
                .storeDurably()
                .build();

        // Testing: Run every 2 minutes
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("OrderConsolidationTrigger", "FruitLinkTriggers")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 * * * ?"))
                .build();

        scheduleIfNotExist(jobDetail, trigger);
    }

    private void scheduleRouteOptimizationJob() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(RouteOptimizationJob.class)
                .withIdentity("RouteOptimizationJob", "FruitLinkJobs")
                .storeDurably()
                .build();

        // Testing: Run every 2 minutes
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("RouteOptimizationTrigger", "FruitLinkTriggers")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 * * * ?"))
                .build();

        scheduleIfNotExist(jobDetail, trigger);
    }

    private void scheduleAgingReceivablesSweepJob() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(AgingReceivablesSweepJob.class)
                .withIdentity("AgingReceivablesSweepJob", "FruitLinkJobs")
                .storeDurably()
                .build();

        // Testing: Run every 2 minutes
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("AgingReceivablesSweepTrigger", "FruitLinkTriggers")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 * * * ?"))
                .build();

        scheduleIfNotExist(jobDetail, trigger);
    }

    private void scheduleIfNotExist(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        if (!scheduler.checkExists(jobDetail.getKey())) {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled job: {}", jobDetail.getKey().getName());
        } else {
            // Update trigger if job already exists (useful if we change the cron expression)
            scheduler.rescheduleJob(trigger.getKey(), trigger);
            log.info("Updated trigger for job: {}", jobDetail.getKey().getName());
        }
    }
}
