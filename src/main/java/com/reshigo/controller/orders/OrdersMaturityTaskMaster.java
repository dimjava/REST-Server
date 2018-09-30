package com.reshigo.controller.orders;

import com.reshigo.service.scheduled.ScheduledTasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Created by dmitry103 on 23/03/17.
 */

@Controller
@Scope("prototype")
public class OrdersMaturityTaskMaster implements Runnable {
    private Long id;

    private final ScheduledTasks scheduledTasks;

    @Autowired
    public OrdersMaturityTaskMaster(ScheduledTasks scheduledTasks) {
        this.scheduledTasks = scheduledTasks;
    }

    @Override
    public void run() {
        scheduledTasks.cleanup(id);
    }

    public void setId(Long id) {
        this.id = id;
    }
}