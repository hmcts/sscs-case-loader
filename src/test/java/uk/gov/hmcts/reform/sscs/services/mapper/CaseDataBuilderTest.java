package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataBuilderTest {

    @Mock
    private ReferenceDataService refDataService;

    private AppealCase appeal;

    private CaseDataBuilder caseDataBuilder;

    @Before
    public void setUp() throws Exception {
        caseDataBuilder = new CaseDataBuilder(refDataService);
        appeal = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .build();
    }

    @Test
    public void shouldBuildBenefitTypeGivenAppealCase() {
        when(refDataService.getBenefitType("1")).thenReturn("A");

        BenefitType benefitType = caseDataBuilder.buildBenefitType(appeal);
        assertThat(benefitType.getCode(), is("A"));
    }

    @Test
    public void shouldBuildSubscriptionsWithAppealCaseNumber() {
        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions();
        assertNotNull("AppellantSubscription is null", subscriptions.getAppellantSubscription());
        assertNotNull("SupporterSubscription is null", subscriptions.getSupporterSubscription());
        String appealNumber = subscriptions.getAppellantSubscription().getTya();
        assertTrue("appealNumber is empty", !"".equals(appealNumber));
        assertTrue("appealNumber length is not 10 digits", appealNumber.length() == 10);
    }
}
