package com.redpill_linpro.component.smb.strategy;

import java.util.Date;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultPollingConsumerPollStrategy;
import org.quartz.CronExpression;

import com.redpill_linpro.component.smb.SmbConsumer;
import com.redpill_linpro.component.smb.SmbEndpoint;

public class CronPollStrategy extends DefaultPollingConsumerPollStrategy {
    @Override
    public boolean begin(Consumer consumer, Endpoint endpoint) {
        log.trace("CronPolicy begin called");

        SmbConsumer smbConsumer = (SmbConsumer) consumer;

        try {
            CronExpression cronExpression = lookup((SmbEndpoint) endpoint);

            Date now = new Date();
            Date nextTrigger = cronExpression.getNextValidTimeAfter(now);
            Long timeSpan = Math.abs(nextTrigger.getTime() - now.getTime());
            
            if (log.isDebugEnabled()) {
                log.debug(String.format("Now [%1$tH:%1$tM:%1$tS] Next [%2$tH:%2$tM:%2$tS]", now, nextTrigger));
                log.debug(String.format("Span [%d] Delay [%d]", timeSpan, smbConsumer.getDelay()));
            }

            return (timeSpan <= smbConsumer.getDelay());
        } catch (Exception e) {
            log.warn("No valid cron expression", e);
            return true;
        }
    }

    public boolean rollback(Consumer consumer, Endpoint endpoint, int retryCounter, Exception cause) throws Exception {
        log.info("strategy got an Exception: " + cause);
        log.info("retryCounter [" + retryCounter + "]");
        throw cause;
    }

    private CronExpression lookup(SmbEndpoint smbEndpoint) throws Exception {
        String cron = smbEndpoint.getCron();

        if (cron == null) throw new Exception("No cron expression available");
        if (!CronExpression.isValidExpression(cron)) throw new Exception("Invalid cron expression [" + cron + "]");

        return new CronExpression(cron);
    }
}
