package uk.gov.hmcts.reform.sscs.util;

import java.time.LocalDateTime;
import java.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

@Component
@Slf4j
public class CaseLoaderTimerTask extends TimerTask {

    private final SftpSshService sftpSshService;

    @Autowired
    public CaseLoaderTimerTask(SftpSshService sftpSshService) {
        super();
        this.sftpSshService = sftpSshService;
    }

    @Override
    public void run() {
        sftpSshService.closeChannelAdapter();
        log.info("Case loader shutdown performed on: " + LocalDateTime.now()
                + " Thread's name: " + Thread.currentThread().getName());
    }
}
