package uk.gov.hmcts.reform.sscs.scheduler;

import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;

@RunWith(JUnitParamsRunner.class)
public class CronExpressionProductionTest {

    private static final String PRODUCTION_CRON_EXPRESSION = "0 0 09-16/1 * * MON-FRI";

    @Test
    @Parameters({"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"})
    public void givenCronExpressionForProduction_shouldRunOnlyFromMondayToFriday(DayOfWeek dayOfWeek) {
        CronTrigger trigger = new CronTrigger(PRODUCTION_CRON_EXPRESSION);
        final Date today = java.sql.Date.valueOf(LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(dayOfWeek)));
        Date nextExecutionTime = trigger.nextExecutionTime(
            new TriggerContext() {

                @Override
                public Date lastScheduledExecutionTime() {
                    return today;
                }

                @Override
                public Date lastActualExecutionTime() {
                    return today;
                }

                @Override
                public Date lastCompletionTime() {
                    return today;
                }
            });

        LocalDate nextExecution = new java.sql.Date(nextExecutionTime.getTime()).toLocalDate();
        assertTrue("cannot run in Saturday", nextExecution.getDayOfWeek() != DayOfWeek.SATURDAY);
        assertTrue("cannot run in Sunday", nextExecution.getDayOfWeek() != DayOfWeek.SUNDAY);

    }
}
