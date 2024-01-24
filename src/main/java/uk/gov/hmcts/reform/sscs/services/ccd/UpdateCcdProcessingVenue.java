package uk.gov.hmcts.reform.sscs.services.ccd;

import feign.FeignException;
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

            try{
                log.info("Processing venue has changed from {} to {} for case {}", existingProcessingVenue,
                    gapsProcessingVenue, existingCcdCaseData.getCcdCaseId());

                existingCcdCaseData.setProcessingVenue(gapsProcessingVenue);

                venueUpdated = true;

            } catch (FeignException e) {
                log.info("Could not update processing venue from {} to {} for case {}", existingProcessingVenue,
                    gapsProcessingVenue, existingCcdCaseData.getCcdCaseId());
            }

        } else {
            log.info("Processing venue has not changed for case {} . Processing venue =  {}",
                existingCcdCaseData.getCcdCaseId(), existingProcessingVenue);
        }

        return venueUpdated;
    }
}
