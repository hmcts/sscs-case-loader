package uk.gov.hmcts.reform.sscs.services;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.sscs.util.MigrationStringUtils.decompressAndB64Decode;

@Service
@Slf4j
public class ProcessingVenueMigrationService {

    private final CcdCasesSender ccdCasesSender;
    private final IdamService idamService;

    @Value("${features.venue-migration.encoded-data-string}")
    private String encodedDataString;

    public ProcessingVenueMigrationService(CcdCasesSender ccdCasesSender, IdamService idamService) {
        this.ccdCasesSender = ccdCasesSender;
        this.idamService = idamService;
    }

    public void process(String venue) throws IOException {
        JSONArray data = new JSONArray(decompressAndB64Decode(encodedDataString));
        AtomicInteger unprocessed = new AtomicInteger(data.length());
        log.info("Number of cases to be migrated: ({})", unprocessed.get());
        data.iterator().forEachRemaining(row -> {
            boolean success = ccdCasesSender.updateProcessingVenue(
                ((JSONObject) row).getLong("reference"),
                idamService.getIdamTokens(),
                ((JSONObject) row).getString(venue).trim()
            );
            if (success) {
                unprocessed.decrementAndGet();
            }
        });
        log.info("Number of unprocessed cases: ({})", unprocessed.get());
    }
}
