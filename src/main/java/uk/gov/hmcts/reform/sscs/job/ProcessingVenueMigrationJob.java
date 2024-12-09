package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.DORMANT_APPEAL_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.VOID_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appointee;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.model.CourtVenue;
import uk.gov.hmcts.reform.sscs.service.RefDataService;
import uk.gov.hmcts.reform.sscs.service.RegionalProcessingCenterService;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.services.DataMigrationService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public class ProcessingVenueMigrationJob extends DataMigrationJob {


    @Value("${features.venue-migration.enabled}")
    private boolean venueDataMigrationEnabled;

    @Value("${features.venue-migration.encoded-data-string}")
    private String venueEncodedDataString;

    @Value("${features.venue-migration.startHour}")
    private int migrationStartHour;

    @Value("${features.venue-migration.endHour}")
    private int migrationEndHour;

    @Value("${features.venue-migration.rollback}")
    public boolean isVenueRollback;


    private final VenueService venueService;

    private final RefDataService refDataService;

    private final RegionalProcessingCenterService regionalProcessingCenterService;



    public ProcessingVenueMigrationJob(CaseLoaderTimerTask caseLoaderTimerTask,
                                       DataMigrationService migrationService,
                                       VenueService venueService,
                                       RefDataService refDataService,
                                       RegionalProcessingCenterService regionalProcessingCenterService) {
        super(caseLoaderTimerTask, migrationService);
        this.venueService = venueService;
        this.refDataService = refDataService;
        this.regionalProcessingCenterService = regionalProcessingCenterService;
    }

    @Override
    public boolean readyToRun() {
        return venueDataMigrationEnabled
            && now().getHour() >= migrationStartHour
            && now().getHour() <= migrationEndHour;
    }

    @Override
    String getEncodedDataString() {
        return venueEncodedDataString;
    }

    @Override
    boolean isRollback() {
        return isVenueRollback;
    }

    public void updateCaseData(SscsCaseData caseData, String venue) {
        String postCode = resolvePostCode(caseData);
        RegionalProcessingCenter newRpc = regionalProcessingCenterService.getByPostcode(postCode, false);
        String venueEpimsId = venueService.getEpimsIdForVenue(venue);
        log.info("Epims id for case {}: {}", caseData.getCaseReference(), venueEpimsId);
        CourtVenue courtVenue = refDataService.getCourtVenueRefDataByEpimsId(venueEpimsId);
        log.info("Setting processing venue to ({}), RPC to ({}), and Case management location to region ({})",
            venue, newRpc.getName(), courtVenue.getRegionId());
        caseData.setCaseManagementLocation(CaseManagementLocation.builder()
            .baseLocation(newRpc.getEpimsId())
            .region(courtVenue.getRegionId()).build());
        caseData.setRegionalProcessingCenter(newRpc);
        caseData.setRegion(newRpc.getName());
        caseData.setProcessingVenue(venue);

    }

    public boolean shouldBeSkipped(SscsCaseDetails caseDetails, String venue) {
        var isInExcludedState = caseDetails.getState().equals(VOID_STATE.toString())
            || caseDetails.getState().equals(DORMANT_APPEAL_STATE.toString());
        boolean shouldBeSkipped = venue.equals(caseDetails.getData().getProcessingVenue())
            || isInExcludedState;
        if (shouldBeSkipped) {
            log.info(
                "Skipping case ({}) because venue already set ({})  or state={}",
                caseDetails.getId(), venue,
                caseDetails.getState()
            );
        }
        return shouldBeSkipped;
    }

    private static String resolvePostCode(SscsCaseData sscsCaseData) {
        if (YES.getValue().equalsIgnoreCase(sscsCaseData.getAppeal().getAppellant().getIsAppointee())) {
            return Optional.ofNullable(sscsCaseData.getAppeal().getAppellant().getAppointee())
                .map(Appointee::getAddress)
                .map(Address::getPostcode)
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .orElse(sscsCaseData.getAppeal().getAppellant().getAddress().getPostcode());
        }

        return sscsCaseData.getAppeal().getAppellant().getAddress().getPostcode();
    }

}
