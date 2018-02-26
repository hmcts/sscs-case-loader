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

    private final CoreCaseDataServiceUtil coreCaseDataServiceUtil;

    @Autowired
    public CreateCoreCaseDataService(CoreCaseDataServiceUtil coreCaseDataServiceUtil) {
        this.coreCaseDataServiceUtil = coreCaseDataServiceUtil;
    }

    public CaseDetails createCcdCase(CaseData caseData) {
        log.info("createCcdCase...");
        EventRequestData eventRequestData = coreCaseDataServiceUtil.getEventRequestData("appealCreated");
        String serviceAuthorization = coreCaseDataServiceUtil.generateServiceAuthorization();
        StartEventResponse startEventResponse = start(eventRequestData, serviceAuthorization);
        return create(eventRequestData, serviceAuthorization, coreCaseDataServiceUtil.getCaseDataContent(caseData,
            startEventResponse, "SSCS - appeal created event", "Created SSCS"));
    }

    private StartEventResponse start(EventRequestData eventRequestData, String serviceAuthorization) {
        log.info("start...");
        return coreCaseDataServiceUtil.getCoreCaseDataApi().startForCaseworker(
            eventRequestData.getUserToken(),
            serviceAuthorization,
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId());
    }

    private CaseDetails create(EventRequestData eventRequestData, String serviceAuthorization,
                               CaseDataContent caseDataContent) {
        log.info("create...");
        return coreCaseDataServiceUtil.getCoreCaseDataApi().submitForCaseworker(
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
