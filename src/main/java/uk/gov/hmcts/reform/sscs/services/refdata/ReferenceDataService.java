package uk.gov.hmcts.reform.sscs.services.refdata;

import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BAT_CODE_MAP;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BEN_ASSESS_TYPE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.CASE_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.FUR_EVID_TYPE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.PTTP_ROLE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BAT_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BENEFIT_DESC;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BEN_ASSESS_TYPE_ID;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.FET_DESC;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.PTR_DESC;

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
}
