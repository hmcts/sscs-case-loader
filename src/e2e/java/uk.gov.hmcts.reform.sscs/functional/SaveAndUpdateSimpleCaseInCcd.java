package uk.gov.hmcts.reform.sscs.functional;

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
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdApiWrapper;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class SaveAndUpdateSimpleCaseInCcd {

    @Autowired
    private CcdApiWrapper ccdApiWrapper;

    @Test
    public void shouldBeSavedAndThenUpdatedIntoCcdGivenACase() {
        CaseData caseData = CaseDataUtils.buildCaseData("SC068/17/00013");
        CaseDetails caseDetails = ccdApiWrapper.create("appealReceived", caseData);
        assertNotNull(caseDetails);
        CaseData updatedCaseData = CaseDataUtils.buildCaseData("SC123/12/78765");
        CaseDetails updatedCaseDetails = ccdApiWrapper.update(updatedCaseData, caseDetails.getId(),
            "appealReceived");
        assertEquals("SC123/12/78765", updatedCaseDetails.getData().get("caseReference"));
    }

}
