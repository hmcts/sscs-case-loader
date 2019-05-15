package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.models.UpdateType;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdCaseDataTest {

    private UpdateCcdCaseData updateCcdCaseData;
    @Mock
    private UpdateCcdAppellantData updateCcdAppellantData;
    @Mock
    private UpdateCcdHearingOptions updateCcdHearingOptions;
    @Mock
    private UpdateCcdHearingType updateCcdHearingType;
    @Mock
    private UpdateGeneratedFields updateGeneratedFields;
    @Mock
    private UpdateDwpTimeExtension updateDwpTimeExtension;
    @Mock
    private UpdateEvents updateEvents;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        updateCcdCaseData = new UpdateCcdCaseData(updateCcdAppellantData, updateCcdHearingOptions,
            updateCcdHearingType, updateGeneratedFields, updateDwpTimeExtension, updateEvents);
    }

    @Test
    public void givenAChangeInRep_shouldUpdateTypeAccordingly() {
        Name gapsName = Name.builder().lastName("Potter").build();
        Appeal gapsAppeal = Appeal.builder().rep(Representative.builder().name(gapsName).build()).build();
        Name existingName = Name.builder().lastName("Superman").build();
        Appeal existingAppeal = Appeal.builder().rep(Representative.builder().name(existingName).build()).build();
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(gapsAppeal)
            .build();
        SscsCaseData existingCase = gapsCaseData.toBuilder().appeal(existingAppeal).build();

        UpdateType updateType = updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(
            gapsCaseData, existingCase);

        assertThat(updateType, is(UpdateType.DATA_UPDATE));
    }

    @Test
    public void givenAChangeInRep_shouldNotUnsubscribeRepOrAppellant() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder().rep(Representative.builder()
                .name(Name.builder().lastName("Potter").build()).build()).build())
            .subscriptions(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                    .subscribeEmail("No").email("harry.potter@mail.com").build())
                .appellantSubscription(Subscription.builder()
                    .email("appellant@email.com").subscribeEmail("No").build())
                .build())
            .build();

        SscsCaseData existingCase = gapsCaseData.toBuilder()
            .appeal(
                Appeal.builder()
                    .rep(Representative.builder()
                        .name(Name.builder().lastName("Superman").build()).build())
                .build())
            .subscriptions(Subscriptions.builder()
                .appellantSubscription(Subscription.builder()
                    .email("appellant@email.com").subscribeEmail("Yes").build())
                .representativeSubscription(Subscription.builder()
                    .subscribeEmail("Yes").email("superman@mail.com").build())
                .build()
            )
            .build();

        final Subscription expectedAppellantSubscription = Subscription.builder()
            .email("appellant@email.com").subscribeEmail("Yes").build();

        final Subscription expectedRepSubscription = Subscription.builder()
            .email("harry.potter@mail.com").subscribeSms("No").subscribeEmail("Yes").build();

        UpdateType updateType = updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(
            gapsCaseData, existingCase);

        assertEquals(UpdateType.DATA_UPDATE, updateType);
        assertEquals(expectedAppellantSubscription, existingCase.getSubscriptions().getAppellantSubscription());
        assertEquals(expectedRepSubscription, existingCase.getSubscriptions().getRepresentativeSubscription());
        assertNull(existingCase.getSubscriptions().getAppointeeSubscription());

    }

    @Test
    @Parameters(method = "generateAppealScenarios")
    public void givenCaseChange_shouldReturnUpdateTypeAccordingly(SscsCaseData gapsCaseData,
                                                                  boolean updateEvent,
                                                                  boolean updateAppellant,
                                                                  UpdateType expectedUpdateType) {
        given(updateEvents.update(any(), any())).willReturn(updateEvent);

        given(updateCcdAppellantData.updateCcdAppellantData(any(), any())).willReturn(updateAppellant);
        given(updateDwpTimeExtension.updateDwpTimeExtension(any(), any())).willReturn(false);
        given(updateCcdHearingOptions.updateHearingOptions(any(), any(SscsCaseData.class))).willReturn(false);
        given(updateCcdHearingType.updateHearingType(any(), any())).willReturn(false);

        UpdateType updateType = updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(
            gapsCaseData, null);

        assertThat(updateType, is(expectedUpdateType));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateAppealScenarios() {
        SscsCaseData sscsCaseDataWithNullAppeal = SscsCaseData.builder()
            .appeal(null)
            .build();
        SscsCaseData sscsCaseDataWithAppeal = SscsCaseData.builder()
            .appeal(Appeal.builder().build())
            .build();

        return new Object[]{
            new Object[]{null, true, true, UpdateType.EVENT_UPDATE},
            new Object[]{null, true, false, UpdateType.EVENT_UPDATE},
            new Object[]{sscsCaseDataWithNullAppeal, true, true, UpdateType.EVENT_UPDATE},
            new Object[]{sscsCaseDataWithNullAppeal, true, false, UpdateType.EVENT_UPDATE},
            new Object[]{sscsCaseDataWithNullAppeal, false, true, UpdateType.NO_UPDATE},
            new Object[]{sscsCaseDataWithAppeal, false, true, UpdateType.DATA_UPDATE}
        };
    }

}
