package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.*;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.*;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdRepresentativeTest {
    private static final String ABCDEFGH_1 = "abcdefgh1";
    private static final String ABCDEFGH_2 = "ABCDEFGH2";

    @Test
    public void givenARepChangeFromNullRep_willChangeDataAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder()
                    .name(Name.builder()
                        .lastName("Potter")
                        .build())
                    .hasRepresentative(YES)
                    .build()
                ).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder().build()).build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);
        assertTrue("data has changed from a null rep", hasDataChanged);
        assertNull(existingCaseData.getAppeal().getRep().getName());
        assertNotEquals(gapsCaseData.getAppeal().getRep(), existingCaseData.getAppeal().getRep());
        assertNotNull(existingCaseData.getSubscriptions().getRepresentativeSubscription());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isEmailSubscribed());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isSmsSubscribed());
    }

    @Test
    @Parameters({
        "Potter,Superman",
        "Potter,",
        ",Superman"
    })
    public void givenARepNameChange_willNotChangeDataAndReturnTrue(String gapsSurname, String existingSurname) {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName(gapsSurname).build())
                        .hasRepresentative(YES).build()
                ).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName(existingSurname).build())
                    .hasRepresentative(YES).build()
            ).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertTrue("rep name has not changed", hasDataChanged);
        assertEquals(YES, existingCaseData.getAppeal().getRep().getHasRepresentative());
        assertEquals(existingSurname, existingCaseData.getAppeal().getRep().getName().getLastName());
        assertNotNull(existingCaseData.getSubscriptions().getRepresentativeSubscription());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isEmailSubscribed());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isSmsSubscribed());
    }

    @Test
    public void givenARepNameChangeWhenExistingNameIsNull_willChangeDataAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .hasRepresentative(YES).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(
                    Subscription.builder().tya(ABCDEFGH_1).email("harry@potter.com").subscribeEmail(NO)
                            .subscribeSms(NO).build()).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(null).build()
            ).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertTrue("rep name has not changed", hasDataChanged);
        assertEquals(NO, existingCaseData.getAppeal().getRep().getHasRepresentative());
        assertNull(existingCaseData.getAppeal().getRep().getName());
        assertNotNull(existingCaseData.getSubscriptions().getRepresentativeSubscription());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isEmailSubscribed());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isSmsSubscribed());
    }

    @Test
    public void givenARepContactChange_willChangeDataButKeepExistingTyaAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .hasRepresentative(YES)
                        .contact(Contact.builder().email("harry@potter.com").build()).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(
                Subscription.builder().tya(ABCDEFGH_1).email("harry@potter.com").subscribeEmail(NO)
                        .subscribeSms(NO).build()).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .hasRepresentative(YES)
                        .contact(Contact.builder().email("harry.potter@wizards.com").build()).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(
                Subscription.builder().tya(ABCDEFGH_2).email("harry.potter@wizards.com").subscribeEmail(NO)
                        .subscribeSms(NO).build()).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);
        assertTrue("rep contact has changed", hasDataChanged);
        Assertions.assertThat(existingCaseData.getAppeal().getRep())
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(gapsCaseData.getAppeal().getRep());
        assertEquals(ABCDEFGH_2, existingCaseData.getSubscriptions().getRepresentativeSubscription().getTya());
        assertEquals("harry.potter@wizards.com",
                existingCaseData.getSubscriptions().getRepresentativeSubscription().getEmail());
    }

    @Test
    public void givenARepContactChangeWhenExistingContactIsNull_willChangeDataButKeepExistingTyaAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .contact(Contact.builder().email("harry@potter.com").build()).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(
                Subscription.builder().tya(ABCDEFGH_1).email("harry@potter.com").subscribeEmail(NO)
                    .subscribeSms(NO).build()).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Potter").build())
                    .contact(null).build()
            ).build())
                .subscriptions(Subscriptions.builder().representativeSubscription(
                        Subscription.builder().tya(ABCDEFGH_2).email("harry.potter@wizards.com").subscribeEmail(NO)
                                .subscribeSms(NO).build()).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);
        assertTrue("rep contact has changed", hasDataChanged);
        assertEquals(ABCDEFGH_2, existingCaseData.getSubscriptions().getRepresentativeSubscription().getTya());
        assertEquals("harry.potter@wizards.com",
                existingCaseData.getSubscriptions().getRepresentativeSubscription().getEmail());

    }

    @Test
    public void givenARepInvalidUkMobile_willNotChangeExistingMobileAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .hasRepresentative(YES)
                        .contact(Contact.builder().email("harry@potter.com").mobile("INVALID")
                            .build()).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(
                Subscription.builder().email("harry@potter.com").subscribeEmail(NO).wantSmsNotifications(NO)
                    .subscribeSms(NO).build()).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Potter").build())
                    .hasRepresentative(YES)
                    .contact(Contact.builder().mobile("07123456789").build()).build()
            ).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);
        assertTrue("rep contact has changed", hasDataChanged);

        SscsCaseData expectedExistingRepsData = gapsCaseData.toBuilder().build();
        expectedExistingRepsData.getAppeal().getRep().getContact().setMobile("07123456789");
        Assertions.assertThat(existingCaseData.getAppeal().getRep())
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(gapsCaseData.getAppeal().getRep());

        assertEquals(gapsCaseData.getSubscriptions().getRepresentativeSubscription(),
            existingCaseData.getSubscriptions().getRepresentativeSubscription());
    }

    @Test
    public void givenARepValidUkMobile_willChangeExistingMobileAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .hasRepresentative(YES)
                        .contact(Contact.builder().email("harry@potter.com").mobile("07111111111")
                            .build()).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(
                Subscription.builder().email("harry@potter.com").subscribeEmail(NO)
                    .wantSmsNotifications(NO)
                    .subscribeSms(NO).build()).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Potter").build())
                    .hasRepresentative(YES)
                    .contact(Contact.builder().mobile("07123456789").build()).build()
            ).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);
        assertTrue("rep contact has changed", hasDataChanged);
        Assertions.assertThat(existingCaseData.getAppeal().getRep())
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(gapsCaseData.getAppeal().getRep());

        assertEquals(gapsCaseData.getAppeal().getRep().getContact().getMobile(),
            existingCaseData.getAppeal().getRep().getContact().getMobile());

        assertEquals(gapsCaseData.getSubscriptions().getRepresentativeSubscription(),
            existingCaseData.getSubscriptions().getRepresentativeSubscription());
    }

    @Test
    public void givenNoRepChange_willReturnFalseAndNotModifyData() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .contact(Contact.builder().email("harry@potter.com").build()).build()
                ).build())
            .build();

        SscsCaseData existingCaseData = gapsCaseData.toBuilder().build();
        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertFalse(hasDataChanged);
        Assertions.assertThat(existingCaseData)
            .usingRecursiveComparison()
            .ignoringFields("jointParty")
            .isEqualTo(gapsCaseData);
    }

    @Test
    public void givenNullRepChangeWithoutSubscription_willReturnFalseAndNotModifyData() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder().build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Potter").build()).hasRepresentative(YES).build()
            ).build())
            .build();
        SscsCaseData originalExistingCaseData = existingCaseData.toBuilder().build();
        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertFalse(hasDataChanged);
        Assertions.assertThat(existingCaseData)
            .usingRecursiveComparison()
            .ignoringFields("jointParty")
            .isEqualTo(originalExistingCaseData);
    }

    @Test
    public void givenNullRepChangeWhenHadRepSubscription_willReturnFalseAndNotModifyData() {
        SscsCaseData newCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder().build())
            .build();

        SscsCaseData ccdCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder()
                    .name(Name.builder()
                        .lastName("Potter")
                        .build())
                    .build())
                .build())
            .subscriptions(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                    .wantSmsNotifications(YES)
                    .subscribeEmail(YES)
                    .email("test@test.com")
                    .subscribeSms(YES)
                    .mobile("07811111111")
                    .build())
                .build())
            .build();
        SscsCaseData expectedCaseData = ccdCaseData.toBuilder()
            .subscriptions(
                Subscriptions.builder()
                    .representativeSubscription(
                        Subscription.builder()
                            .wantSmsNotifications(NO)
                            .subscribeEmail(NO)
                            .email("")
                            .subscribeSms(NO)
                            .mobile("")
                            .build()
                    )
                    .build()
            )
            .build();
        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(newCaseData, ccdCaseData);

        assertTrue(hasDataChanged);
        Assertions.assertThat(ccdCaseData)
            .usingRecursiveComparison()
            .ignoringFields("jointParty")
            .isEqualTo(expectedCaseData);
    }

    @Test
    public void givenNullAppeal_willReturnFalseAndNotModifyData() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Potter").build()).build()
            ).build())
            .build();
        SscsCaseData originalExistingCaseData = existingCaseData.toBuilder().build();
        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertFalse(hasDataChanged);
        Assertions.assertThat(existingCaseData)
            .usingRecursiveComparison()
            .ignoringFields("jointParty")
            .isEqualTo(originalExistingCaseData);
    }

    @Test
    @Parameters({"Yes", "No"})
    public void givenARepChange_willKeepExistingEmailAndSmsSubscriptions(String subscribed) {
        YesNo yesNoSubscribed = isYesOrNo(subscribed);
        YesNo oppositeOfSubscribed = isYesOrNo(isNoOrNull(yesNoSubscribed));
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build()).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(Subscription.builder()
                .subscribeSms(oppositeOfSubscribed)
                .subscribeEmail(oppositeOfSubscribed)
                .wantSmsNotifications(oppositeOfSubscribed)
                .mobile("07123456700")
                .email("update@email.com")
                .build()).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Superman").build()).build()
            ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(Subscription.builder()
                .subscribeSms(yesNoSubscribed)
                .subscribeEmail(yesNoSubscribed)
                .wantSmsNotifications(yesNoSubscribed)
                .mobile("07123456711")
                .email("rep@mail.com")
                .build()).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertTrue("representative has changed", hasDataChanged);
        Subscription updatedRepSubscription = existingCaseData.getSubscriptions().getRepresentativeSubscription();
        assertEquals(yesNoSubscribed, updatedRepSubscription.getSubscribeEmail());
        assertEquals(yesNoSubscribed, updatedRepSubscription.getSubscribeSms());
        assertEquals("rep@mail.com", updatedRepSubscription.getEmail());
        assertEquals("07123456700", updatedRepSubscription.getMobile());
    }

    @Test
    @Parameters(method = "generateEdgeScenariosForGapsRepsData")
    public void givenSomeDataInTheGapsRepsIsNullOrEmpty_shouldNotOverwriteExistingRepsDetails(
        SscsCaseData gapsCaseData) {

        SscsCaseData existingCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder()
                    .contact(Contact.builder()
                        .mobile("0770")
                        .build())
                    .name(Name.builder()
                        .firstName("Potter")
                        .build())
                    .address(Address.builder()
                        .town("London")
                        .build())
                    .build())
                .build())
            .build();

        boolean actualUpdateRep = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertTrue(actualUpdateRep);
        assertEquals("0770", existingCaseData.getAppeal().getRep().getContact().getMobile());
        assertEquals("Potter", existingCaseData.getAppeal().getRep().getName().getFirstName());
        assertEquals("London", existingCaseData.getAppeal().getRep().getAddress().getTown());
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateEdgeScenariosForGapsRepsData() {
        SscsCaseData gapsCaseDataWithNullData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder().build())
                .build())
            .build();

        SscsCaseData gapsCaseDataWithNullMobile = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder()
                    .contact(Contact.builder()
                        .build())
                    .build())
                .build())
            .build();

        return new Object[]{
            new Object[]{gapsCaseDataWithNullData},
            new Object[]{gapsCaseDataWithNullMobile}
        };
    }

    @Test
    public void givenInvalidMobileNumberInGapsRepsSubscription_shouldNotOverwriteMobileInExistingRepsSubscription() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder()
                    .name(Name.builder()
                        .lastName("Potter")
                        .build())
                    .contact(Contact.builder()
                        .email("harry@potter.com")
                        .mobile("INVALID")
                        .build())
                    .build())
                .build())
            .subscriptions(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                    .mobile("INVALID")
                    .subscribeSms(YES)
                    .build())
                .build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder()
                    .name(Name.builder()
                        .lastName("Potter")
                        .build())
                    .contact(Contact.builder()
                        .mobile("07123456789")
                        .build())
                    .build())
                .build())
            .subscriptions(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                    .mobile("07123456789")
                    .subscribeSms(NO)
                    .build())
                .build())
            .build();

        boolean actualUpdateRep = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertTrue(actualUpdateRep);
        assertEquals("07123456789",
            existingCaseData.getSubscriptions().getRepresentativeSubscription().getMobile());
        assertEquals(NO, existingCaseData.getSubscriptions().getRepresentativeSubscription().getSubscribeSms());
    }
}
