package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.services.mapper.CaseDataBuilder.NO;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataBuilderTest extends CaseDataBuilderBaseTest {

    @Mock
    private ReferenceDataService refDataService;
    @Mock
    private CaseDataEventBuilder caseDataEventBuilder;
    private CaseDataBuilder caseDataBuilder;
    private AppealCase appeal;

    @Before
    public void setUp() {
        appeal = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(buildMajorStatusGivenStatuses(GapsEvent.APPEAL_RECEIVED))
            .hearing(getHearing())
            .minorStatus(Collections.singletonList(
                super.getMinorStatusGivenIdAndDate("26", ZonedDateTime.parse(TEST_DATE2))))
            .build();
        caseDataBuilder = new CaseDataBuilder(refDataService, caseDataEventBuilder);
    }

    public List<uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing> getHearing() {
        uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing hearing =
            new uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing("outcome",
                "venue",
                "outcomeDate",
                "notificationDate",
                "2017-05-24T00:00:00+01:00",
                "2017-05-24T10:30:00+01:00",
                "id");
        return newArrayList(hearing);
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

    @Test
    public void shouldBuildHearingsGivenAppealWithHearingBookedStatus() {
        VenueDetails venue = VenueDetails.builder()
            .venName("name")
            .build();

        when(refDataService.getVenueDetails("venue")).thenReturn(venue);

        List<Hearing> hearings = caseDataBuilder.buildHearings(appeal);

        Hearing hearing = hearings.get(0);

        assertThat(hearing.getValue().getAdjourned(), is(NO));
        assertThat(hearing.getValue().getHearingDate(), is("2017-05-24"));
        assertThat(hearing.getValue().getTime(), is("10:30:00"));
        assertThat(hearing.getValue().getVenue().getName(), is("name"));
    }

}
