package uk.gov.hmcts.reform.sscs.services.refdata;

import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.*;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.*;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.refdata.VenueDataLoader;

@Service
@Slf4j
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
        String benefitType;
        try {
            benefitType = refDataRepo.find(BAT_CODE_MAP, batCode, BENEFIT_DESC);
        } catch (Exception e) {
            log.debug("Oops...Not found benefitType", e);
            return "ERR";
        }
        return benefitType;
    }

    public String getEvidenceType(String typeOfEvidenceId) {
        return refDataRepo.find(FUR_EVID_TYPE, typeOfEvidenceId, FET_DESC);
    }

    public String getRoleType(String roleId) {
        return refDataRepo.find(PTTP_ROLE, roleId, PTR_DESC);
    }

    public void setRefDataRepo(RefDataRepository repo) {
        this.refDataRepo = repo;
    }

    public String getTbtCode(String tribunalTypeId) {
        return refDataRepo.find(TRIBUNAL_TYPE, tribunalTypeId, TBT_CODE);
    }
}
