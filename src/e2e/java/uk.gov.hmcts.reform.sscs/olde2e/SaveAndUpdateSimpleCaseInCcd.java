package uk.gov.hmcts.reform.sscs.olde2e;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCcdService;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class SaveAndUpdateSimpleCaseInCcd {

    @Autowired
    private CreateCcdService createCcdService;
    @Autowired
    private UpdateCcdService updateCcdService;
    @Autowired
    private IdamService idamService;

    @Test
    public void shouldBeSavedAndThenUpdatedIntoCcdGivenACase() {
        CaseData caseData = CaseDataUtils.buildCaseData("SC068/17/00013");
        IdamTokens idamTokens = IdamTokens.builder()
            .authenticationService(idamService.generateServiceAuthorization())
            .idamOauth2Token(idamService.getIdamOauth2Token())
            .build();
        CaseDetails caseDetails = createCcdService.create(caseData, idamTokens);
        assertNotNull(caseDetails);
        CaseData updatedCaseData = CaseDataUtils.buildCaseData("SC123/12/78765");
        CaseDetails updatedCaseDetails = updateCcdService.update(updatedCaseData, caseDetails.getId(),
            "appealReceived", idamTokens);
        assertEquals("SC123/12/78765", updatedCaseDetails.getData().get("caseReference"));
    }

}
