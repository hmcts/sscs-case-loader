package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Slf4j
@Service
class UpdateCcdProcessingVenue {

    boolean updateVenue(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {

        boolean venueUpdated = false;

        if (gapsCaseData == null
            || existingCcdCaseData == null
            || gapsCaseData.getProcessingVenue() == null) {
            return false;
        }

        String gapsProcessingVenue = gapsCaseData.getProcessingVenue();
        String existingProcessingVenue = existingCcdCaseData.getProcessingVenue();

        if (!gapsProcessingVenue.equalsIgnoreCase(existingProcessingVenue)) {

            log.info("Processing venue has changed from {} to {} for case {}", existingProcessingVenue,
                gapsProcessingVenue, existingCcdCaseData.getCcdCaseId());

            existingCcdCaseData.setProcessingVenue(gapsProcessingVenue);

            venueUpdated = true;
        } else {
            log.info("Processing venue has not changed for case {} . Processing venue =  {}",
                existingCcdCaseData.getCcdCaseId(), existingProcessingVenue);
        }

        return venueUpdated;
    }
}
