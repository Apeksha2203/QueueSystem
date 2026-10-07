package com.queue.service;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.*;

/** Runs without a student or staff needing to keep a page open; catches up after restart. */
@WebListener
public class ClosingTimeListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;
    public void contextInitialized(ServletContextEvent event) {
        scheduler=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"queue-closing-time");t.setDaemon(true);return t;});
        scheduler.scheduleWithFixedDelay(()->{
            try{new ReservationService().closeAllDue();}
            catch(Exception error){event.getServletContext().log("Queue closing reconciliation failed; will retry.",error);}
        },0,10,TimeUnit.SECONDS);
    }
    public void contextDestroyed(ServletContextEvent event){if(scheduler!=null)scheduler.shutdownNow();}
}
