package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;

@Service
@Slf4j
public class CreateCoreCaseDataService {

    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public CreateCoreCaseDataService(CoreCaseDataService coreCaseDataService) {
        this.coreCaseDataService = coreCaseDataService;
    }

    public CaseDetails createCcdCase(CaseData caseData) {
        EventRequestData eventRequestData = coreCaseDataService.getEventRequestData("appealCreated");
        log.info("*** case-loader *** eventRequestData: {}", eventRequestData);
        String serviceAuthorization = coreCaseDataService.generateServiceAuthorization();
        log.info("*** case-loader *** s2s token: {}", serviceAuthorization);
        StartEventResponse startEventResponse = start(eventRequestData, serviceAuthorization);
        log.info("*** case-loader *** startEventResponse: {}", startEventResponse);
        return save(eventRequestData, serviceAuthorization, coreCaseDataService.getCaseDataContent(caseData,
            startEventResponse, "SSCS - appeal created event", "Created SSCS"));
    }

    private StartEventResponse start(EventRequestData eventRequestData, String serviceAuthorization) {
        String ccdUrl = coreCaseDataService.getCoreCaseDataProperties().getApi().getUrl();
        log.info("*** case-loader *** Calling CCD (url: {}) endpoint to start Case For Caseworker...", ccdUrl);
        return coreCaseDataService.getCoreCaseDataApi().startForCaseworker(
            eventRequestData.getUserToken(),
            serviceAuthorization,
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId());
    }

    private CaseDetails save(EventRequestData eventRequestData, String serviceAuthorization,
                             CaseDataContent caseDataContent) {
        log.info("*** case-loader *** Calling CCD endpoint to save CaseDetails For CaseWorker...");
        return coreCaseDataService.getCoreCaseDataApi().submitForCaseworker(
            eventRequestData.getUserToken(),
            serviceAuthorization,
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }
}
