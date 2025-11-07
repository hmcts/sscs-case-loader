package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.service.VenueService;

@Slf4j
@Service
class UpdateCcdProcessingVenue {

    private final VenueService venueService;

    UpdateCcdProcessingVenue(VenueService venueService) {
        this.venueService = venueService;
    }


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
            return venueUpdatedFromGaps(gapsProcessingVenue, existingProcessingVenue, existingCcdCaseData);
        } else {
            log.info("Processing venue has not changed for case {} . Processing venue =  {}",
                existingCcdCaseData.getCcdCaseId(), existingProcessingVenue);
        }

        return venueUpdated;
    }

    private boolean venueUpdatedFromGaps(String gapsProcessingVenue, String existingProcessingVenue, SscsCaseData existingCcdCaseData) {
        try {
            String venueEpimsId = venueService.getEpimsIdForVenue(gapsProcessingVenue);
            VenueDetails newVenue = venueService.getVenueDetailsForActiveVenueByEpimsId(venueEpimsId);
            if (nonNull(newVenue) && (isEmpty(newVenue.getLegacyVenue())
                || !Objects.equals(newVenue.getLegacyVenue(), existingProcessingVenue))) {
                log.info("Processing venue has changed from {} to {} for case {}", existingProcessingVenue,
                    gapsProcessingVenue, existingCcdCaseData.getCcdCaseId());

                existingCcdCaseData.setProcessingVenue(gapsProcessingVenue);
                return true;
            } else {
                log.info("Existing venue {} has not been replaced by {} for case {}.",
                    existingProcessingVenue, gapsProcessingVenue, existingCcdCaseData.getCcdCaseId());
                return false;
            }

        } catch (Exception e) {
            log.warn("Invalid venue {} for case {}, venue has not been updated.", gapsProcessingVenue, existingCcdCaseData.getCcdCaseId());
            return false;
        }
    }
}
