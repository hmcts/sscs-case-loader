package uk.gov.hmcts.reform.sscs.job;

import static java.sql.Timestamp.valueOf;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @Parameters({"MONDAY, 9, 0", "TUESDAY, 7, 30", "WEDNESDAY, 16, 0", "THURSDAY, 9, 0", "FRIDAY, 8, 59",
        "SATURDAY, 8, 0", "SUNDAY, 14, 0"})
    public void givenCronExpressionForProduction_shouldRunOnlyFromMondayToFridayFrom9To16(
        DayOfWeek dayOfWeek, int hour, int min) {
        CronTrigger trigger = new CronTrigger(PRODUCTION_CRON_EXPRESSION);
        final Date today = valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, min))
            .with(TemporalAdjusters.previousOrSame(dayOfWeek)));
        Date nextExecutionTime = trigger.nextExecutionTime(
            new TriggerContext() {

                @Override
                public Date lastScheduledExecutionTime() {
                    return today;
                }

                @Override
                public Instant lastScheduledExecution() {
                    return null;
                }

                @Override
                public Date lastActualExecutionTime() {
                    return today;
                }

                @Override
                public Instant lastActualExecution() {
                    return null;
                }

                @Override
                public Date lastCompletionTime() {
                    return today;
                }

                @Override
                public Instant lastCompletion() {
                    return null;
                }
            });

        LocalDateTime nextExecution = new Timestamp(nextExecutionTime.getTime()).toLocalDateTime();
        assertTrue("cannot run in Saturday", nextExecution.getDayOfWeek() != DayOfWeek.SATURDAY);
        assertTrue("cannot run in Sunday", nextExecution.getDayOfWeek() != DayOfWeek.SUNDAY);

        assertTrue("has to run at 9 o'clock or after",
            nextExecution.toLocalTime().isAfter(LocalTime.of(8, 59)));
        assertTrue("has to run at 16 o'clcock or earlier ",
            nextExecution.toLocalTime().isBefore(LocalTime.of(16, 1)));
    }
}
