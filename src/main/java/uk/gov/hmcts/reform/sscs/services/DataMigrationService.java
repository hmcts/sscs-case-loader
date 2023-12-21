package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;

import java.util.Base64;

@Service
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
        data.iterator().forEachRemaining(row -> ccdCasesSender.updateLanguage(
            ((JSONObject)row).getLong("reference"),
            idamService.getIdamTokens(),
            ((JSONObject)row).getString(languageColumn).trim()
        ));
    }
}
