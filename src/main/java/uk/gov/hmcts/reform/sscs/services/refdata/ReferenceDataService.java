package uk.gov.hmcts.reform.sscs.services.refdata;

import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.*;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.refdata.VenueDataLoader;

@Service
public class ReferenceDataService {


    private final Map<String, VenueDetails> venueDataMap;
    private RefDataRepository refDataRepo;

    @Autowired
    public ReferenceDataService(VenueDataLoader venueDataLoader) {
        this.venueDataMap = venueDataLoader.getVenueDetailsMap();
    }

    public VenueDetails getVenueDetails(String venueId) {
        return venueDataMap.get(venueId);
    }

    public String getBenefitType(String caseCodeId) {
        String benAssessType = refDataRepo.find(CASE_CODE, caseCodeId, BEN_ASSESS_TYPE_ID);
        String batCode = refDataRepo.find(BEN_ASSESS_TYPE, benAssessType, BAT_CODE);
        return refDataRepo.find(BAT_CODE_MAP, batCode, BENEFIT_DESC);
    }

    public void setRefDataRepo(RefDataRepository repo) {
        this.refDataRepo = repo;
    }
}
