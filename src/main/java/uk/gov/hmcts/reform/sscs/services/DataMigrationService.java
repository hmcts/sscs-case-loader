package uk.gov.hmcts.reform.sscs.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterOutputStream;
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

    public void process(String languageColumn) throws IOException {
        JSONArray data = new JSONArray(decompressAndB64Decode(encodedDataString));
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

    private String decompressAndB64Decode(String b64Compressed) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (OutputStream inflaterOutputStream = new InflaterOutputStream(outputStream)) {
            inflaterOutputStream.write(Base64.getDecoder().decode(b64Compressed));
        }
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }
}
