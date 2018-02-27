package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;

@Service
@Slf4j
public class UpdateCoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataService coreCaseDataService;

    public UpdateCoreCaseDataService(CoreCaseDataApi coreCaseDataApi, CoreCaseDataService coreCaseDataService) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataService = coreCaseDataService;
    }

    public CaseDetails updateCase(CaseData caseData, Long caseId, String eventId) {
        log.info("updateCase...");
        EventRequestData eventRequestData = coreCaseDataService.getEventRequestData(eventId);
        String serviceAuthorization = coreCaseDataService.generateServiceAuthorization();
        StartEventResponse startEventResponse = start(eventRequestData, serviceAuthorization, caseId);
        return submit(eventRequestData, serviceAuthorization, coreCaseDataService.getCaseDataContent(caseData,
            startEventResponse, "SSCS - appeal updated event", "Updated SSCS"), caseId);
    }

    private StartEventResponse start(EventRequestData eventRequestData, String serviceAuthorization, Long caseId) {
        log.info("start...");
        return coreCaseDataApi.startEventForCaseWorker(
            eventRequestData.getUserToken(),
            serviceAuthorization,
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            caseId.toString(),
            eventRequestData.getEventId()
        );
    }

    private CaseDetails submit(EventRequestData eventRequestData, String serviceAuthorization,
                               CaseDataContent caseDataContent, Long caseId) {
        log.info("submit...");
        return coreCaseDataApi.submitEventForCaseWorker(
            eventRequestData.getUserToken(),
            serviceAuthorization,
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            caseId.toString(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }
}
