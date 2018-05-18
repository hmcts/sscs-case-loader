package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Component
@Slf4j
public class CreateCcdService {

    private final CoreCaseDataProperties coreCaseDataProperties;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamService idamService;
    private final StartEventCcdService startEventCcdService;

    @Autowired
    CreateCcdService(CoreCaseDataProperties properties, CoreCaseDataApi ccd, IdamService idamService,
                     StartEventCcdService startEventCcdService) {
        this.coreCaseDataProperties = properties;
        this.coreCaseDataApi = ccd;
        this.idamService = idamService;
        this.startEventCcdService = startEventCcdService;
    }

    @Retryable
    public CaseDetails create(CaseData caseData, IdamTokens idamTokens) {
        return tryCreate(caseData, idamTokens);
    }

    @Recover
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private CaseDetails requestNewTokensAndTryToCreateAgain(CaseData caseData, IdamTokens idamTokens) {
        log.info("*** case-loader *** Requesting new idam and s2s tokens");
        idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
        idamTokens.setServiceAuthorisation(idamService.generateServiceAuthorization());
        return tryCreate(caseData, idamTokens);
    }

    private CaseDetails tryCreate(CaseData caseData, IdamTokens idamTokens) {
        StartEventResponse startEventResponse = startEventCcdService.startCase(idamTokens, "appealCreated");
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("GAPS2 Case")
                .description("CaseLoader Case created")
                .build())
            .data(caseData)
            .build();
        return coreCaseDataApi.submitForCaseworker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorisation(),
            idamTokens.getServiceUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            true,
            caseDataContent);
    }

}
