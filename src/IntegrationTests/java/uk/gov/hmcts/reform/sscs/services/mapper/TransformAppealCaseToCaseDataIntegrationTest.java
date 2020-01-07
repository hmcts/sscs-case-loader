package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.services.mapper.TransformAppealCaseToCaseDataTest.getAppealCase;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
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
    @Parameters({
        "100, 91, 90, No, No, No",
        "110, 91, 90, Yes, No, No",
        "100, 115, 90, No, Yes, No",
        "100, 90, 126, No, No, Yes",
        "200, 90, 126, No, No, Yes",
        "0, 90, 126, No, No, Yes",
        "120, 90, 126, Yes, No, Yes",
        "120, 117, 126, Yes, Yes, Yes",
        ", 117, 126, No, Yes, Yes",
        "null, 117, 126, No, Yes, Yes",
    })
    public void givenHearingAdjournedEvent_shouldSetAdjournedFlagToYes(@Nullable String outcomeId0, String outcomeId1,
                                                                       String outcomeId2,
                                                                       String expectedHearingAdjourned0,
                                                                       String expectedHearingAdjourned1,
                                                                       String expectedHearingAdjourned2) {
        setOutcomeIdValue(outcomeId0, outcomeId1, outcomeId2);

        final SscsCaseData caseData = transformAppealCaseToCaseData.transform(appealCase);

        assertThat(caseData.getHearings().size(), is(3));
        assertThat(caseData.getHearings().get(0).getValue().getAdjourned(), is(expectedHearingAdjourned0));
        assertThat(caseData.getHearings().get(1).getValue().getAdjourned(), is(expectedHearingAdjourned1));
        assertThat(caseData.getHearings().get(2).getValue().getAdjourned(), is(expectedHearingAdjourned2));
    }

    private void setOutcomeIdValue(String outcomeId0, String outcomeId1, String outcomeId2) {
        setOutcomeIdValueForIndex(outcomeId0, 0);
        setOutcomeIdValueForIndex(outcomeId1, 1);
        setOutcomeIdValueForIndex(outcomeId2, 2);
    }

    private void setOutcomeIdValueForIndex(String outcomeId, int index) {
        appealCase.getHearing().set(index, appealCase.getHearing().get(index).toBuilder().outcomeId(outcomeId).build());
    }

    @Test(expected = NumberFormatException.class)
    @Parameters({
        "wrongFormat,125,117",
        ",125,117",
    })
    public void givenHearingAdjournedEventWithWrongOutcomeIdFormat_shouldThrowException(@Nullable String outcomeId0,
                                                                                        String outcomeId1,
                                                                                        String outcomeId2) {
        setOutcomeIdValue(outcomeId0, outcomeId1, outcomeId2);
        transformAppealCaseToCaseData.transform(appealCase);
    }

}
