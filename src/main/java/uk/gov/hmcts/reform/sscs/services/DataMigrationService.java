package uk.gov.hmcts.reform.sscs.services;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;

@Service
@Slf4j
public class DataMigrationService {

    private final CcdCasesSender ccdCasesSender;
    private final IdamService idamService;

    @Value("${features.data-migration.encoded-data-string}")
    private String encodedDataString;

    public DataMigrationService(CcdCasesSender ccdCasesSender, IdamService idamService) {
        this.ccdCasesSender = ccdCasesSender;
        this.idamService = idamService;
    }

    public void process(String languageColumn) {
        String decodedString = new String(Base64.getDecoder().decode(encodedDataString));
        JSONArray data = new JSONArray(decodedString);
        AtomicInteger unprocessed = new AtomicInteger(data.length());
        log.info("Number of cases to be migrated: ({})", unprocessed.get());
        data.iterator().forEachRemaining(row -> {
            boolean success = ccdCasesSender.updateLanguage(
                ((JSONObject) row).getLong("reference"),
                idamService.getIdamTokens(),
                ((JSONObject) row).getString(languageColumn).trim()
            );
            if (success) {
                unprocessed.decrementAndGet();
            }
        });
        log.info("Number of unprocessed cases: ({})", unprocessed.get());
    }
}
