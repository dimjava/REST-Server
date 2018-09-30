package com.reshigo.service.scheduled;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;

/**
 * Created by dmitry103 on 08/06/17.
 */
public interface ScheduledTasks {

    void scheduleOrderMaturity(Long orderId, Date date);

    void cleanup(Long id);

    void cleanFeedList();

    void init();
}
