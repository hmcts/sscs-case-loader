package uk.gov.hmcts.reform.sscs.services;

import static uk.gov.hmcts.reform.sscs.util.MigrationStringUtils.decompressAndB64Decode;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.job.DataMigrationJob;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;

@Service
@Slf4j
public class DataMigrationService {

    private final CcdCasesSender ccdCasesSender;
    private final IdamService idamService;

    public DataMigrationService(CcdCasesSender ccdCasesSender, IdamService idamService) {
        this.ccdCasesSender = ccdCasesSender;
        this.idamService = idamService;
    }

    public void process(String migrationColumn, DataMigrationJob job, String encodedDataString) throws IOException {
        JSONArray data = new JSONArray(decompressAndB64Decode(encodedDataString));
        AtomicInteger unprocessed = new AtomicInteger(data.length());
        log.info("Number of cases to be migrated: ({})", unprocessed.get());
        data.iterator().forEachRemaining(row -> {
            boolean success = ccdCasesSender.updateCaseMigration(
                ((JSONObject) row).getLong("reference"),
                idamService.getIdamTokens(),
                ((JSONObject) row).getString(migrationColumn).trim(),
                job
            );
            if (success) {
                unprocessed.decrementAndGet();
            }
        });
        log.info("Number of unprocessed cases: ({})", unprocessed.get());
    }
}
