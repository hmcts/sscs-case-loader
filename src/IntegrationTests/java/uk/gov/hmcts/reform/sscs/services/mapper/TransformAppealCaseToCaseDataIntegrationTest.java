package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.services.mapper.TransformAppealCaseToCaseDataTest.getAppealCase;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest
public class TransformAppealCaseToCaseDataIntegrationTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @MockBean
    private ReferenceDataService referenceDataService;

    @Autowired
    private TransformAppealCaseToCaseData transformAppealCaseToCaseData;
    private AppealCase appealCase;

    @Before
    public void setUp() throws Exception {
        when(referenceDataService.getBenefitType(anyString())).thenReturn("pip");
        when(referenceDataService.getVenueDetails(anyString())).thenReturn(VenueDetails.builder().build());
        appealCase = getAppealCase("AppealCaseWithAdjournedEventCreatedByOutcomeId.json");
    }

    @Test
    @Parameters({"100, No", "110, Yes", "115, Yes", "126, Yes", "200, No", "0, No"})
    public void givenHearingAdjournedEvent_shouldSetAdjournedFlagToYes(String outcomeId,
                                                                       String expectedHearingAdjourned) {
        setOutcomeIdValue(appealCase, outcomeId);

        final SscsCaseData caseData = transformAppealCaseToCaseData.transform(appealCase);

        assertThat(caseData.getHearings().size(), is(1));
        assertThat(caseData.getHearings().get(0).getValue().getAdjourned(), is(expectedHearingAdjourned));
    }

    private void setOutcomeIdValue(AppealCase appealCase, String outcomeId) {
        appealCase.getHearing().set(0, appealCase.getHearing().get(0).toBuilder().outcomeId(outcomeId).build());
    }

    @Test(expected = NumberFormatException.class)
    public void givenHearingAdjournedEventWithWrongOutcomeIdFormat_shouldThrowException() {
        setOutcomeIdValue(appealCase, "wrongFormat");

        final SscsCaseData caseData = transformAppealCaseToCaseData.transform(appealCase);
    }
}
