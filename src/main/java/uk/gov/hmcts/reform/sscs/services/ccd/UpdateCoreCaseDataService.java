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
public class UpdateCoreCaseDataService {

    private final CoreCaseDataServiceUtil coreCaseDataServiceUtil;

    @Autowired
    public UpdateCoreCaseDataService(CoreCaseDataServiceUtil coreCaseDataServiceUtil) {
        this.coreCaseDataServiceUtil = coreCaseDataServiceUtil;
    }

    public CaseDetails updateCase(CaseData caseData, Long caseId, String eventId) {
        log.info("updateCase...");
        EventRequestData eventRequestData = coreCaseDataServiceUtil.getEventRequestData(eventId);
        String serviceAuthorization = coreCaseDataServiceUtil.generateServiceAuthorization();
        StartEventResponse startEventResponse = start(eventRequestData, serviceAuthorization, caseId);
        return submit(eventRequestData, serviceAuthorization, coreCaseDataServiceUtil.getCaseDataContent(caseData,
            startEventResponse, "SSCS - appeal updated event", "Updated SSCS"), caseId);
    }

    private StartEventResponse start(EventRequestData eventRequestData, String serviceAuthorization, Long caseId) {
        log.info("start...");
        return coreCaseDataServiceUtil.getCoreCaseDataApi().startEventForCaseWorker(
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
        return coreCaseDataServiceUtil.getCoreCaseDataApi().submitEventForCaseWorker(
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
