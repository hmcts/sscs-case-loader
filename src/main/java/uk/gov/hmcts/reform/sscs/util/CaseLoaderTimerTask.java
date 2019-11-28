package uk.gov.hmcts.reform.sscs.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.TimerTask;

@Slf4j
public class CaseLoaderTimerTask extends TimerTask {
    @Override
    public void run() {
        log.info("Case loader shutdown performed on: " + LocalDateTime.now() +
                " Thread's name: " + Thread.currentThread().getName());
    }
}
