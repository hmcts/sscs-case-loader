package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataNoWhenThereAreGapsDataWithNewAppointeeNameHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreExistingCcdDataUpdatesWithEmptyFields;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreExistingCcdDataUpdatesWithNullFields;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataUpdatesWithEmptyFields;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataUpdatesWithNullFields;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithNewAppointeeContactHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithNewAppointeeIdentityHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeEmailHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeFirstNameHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeLastNameHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeMobileHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeNinoHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeePhoneHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithUpdatedMobileHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataWithUpdatedPhoneHappyPaths;

import java.io.IOException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.GapsAndCcdDataUpdateScenario;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.GapsAppellantData;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdAppellantDataTest {

    private static final String CASE_DETAILS_WITH_SUBSCRIPTIONS_JSON = "CaseDetailsWithSubscriptions.json";

    private final UpdateCcdAppellantData updateCcdAppellantData = new UpdateCcdAppellantData();
    private SscsCaseData gapsCaseData;
    private SscsCaseDetails existingCaseDetails;
    private final Name name = Name.builder().lastName("Potter").build();
    private final Name newName = Name.builder().lastName("Superman").build();
    private final String normalisedNino = "AB554466B";

    @Test
    public void givenAChangeInAppointee_shouldNotUnsubscribeAppointeeOrRep() {

        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder().name(name).build())
                .appellant(Appellant.builder().name(name)
                    .appointee(Appointee.builder().name(name).build())
                    .build())
                .build())
            .subscriptions(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                    .subscribeSms("No")
                    .subscribeEmail("No")
                    .wantSmsNotifications("No")
                    .email("harry.potter@mail.com")
                    .build())
                .appointeeSubscription(Subscription.builder()
                    .email("appointee.new@email.com")
                    .subscribeEmail("No")
                    .wantSmsNotifications("No")
                    .build())
                .build())
            .build();

        SscsCaseData existingCase = gapsCaseData.toBuilder()
            .appeal(gapsCaseData.getAppeal().toBuilder()
                .appellant(Appellant.builder().name(name)
                    .appointee(Appointee.builder().name(newName).build()).build())
                .build())
            .subscriptions(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                    .subscribeSms("Yes")
                    .mobile("0777")
                    .subscribeEmail("Yes")
                    .wantSmsNotifications("Yes")
                    .email("harry.potter@mail.com")
                    .build())
                .appointeeSubscription(Subscription.builder()
                    .subscribeSms("Yes")
                    .wantSmsNotifications("Yes")
                    .mobile("0777")
                    .email("appointee.old@email.com")
                    .subscribeEmail("Yes")
                    .build())
                .build()
            ).build();

        final Subscription expectedRepSubscription = Subscription.builder()
            .email("harry.potter@mail.com")
            .subscribeSms("Yes")
            .mobile("0777")
            .wantSmsNotifications("Yes")
            .subscribeEmail("Yes")
            .build();

        final Subscription expectedAppointeeSubscription = Subscription.builder()
            .email("appointee.old@email.com")
            .subscribeSms("Yes")
            .wantSmsNotifications("Yes")
            .mobile("0777")
            .subscribeEmail("Yes")
            .build();

        boolean isUpdated = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCase);
        assertTrue(isUpdated);
        assertEquals(newName, existingCase.getAppeal().getAppellant().getAppointee().getName());
        assertEquals(expectedRepSubscription, existingCase.getSubscriptions().getRepresentativeSubscription());
        assertEquals(expectedAppointeeSubscription, existingCase.getSubscriptions().getAppointeeSubscription());
        assertNull(existingCase.getSubscriptions().getAppellantSubscription());
    }

    @Test
    public void givenAChangeInAppellant_shouldNotUnsubscribeAppellantOrRep() {

        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder().name(name).build())
                .appellant(Appellant.builder().name(newName).build())
                .build())
            .subscriptions(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                    .subscribeSms("No")
                    .subscribeEmail("No")
                    .wantSmsNotifications("No")
                    .email("harry.potter@mail.com")
                    .build())
                .appellantSubscription(Subscription.builder()
                    .email("appellant.new@email.com")
                    .wantSmsNotifications("No")
                    .subscribeEmail("No")
                    .build())
                .build())
            .build();

        SscsCaseData existingCase = gapsCaseData.toBuilder()
            .appeal(gapsCaseData.getAppeal().toBuilder()
                .appellant(Appellant.builder().name(name).build()).build())
            .subscriptions(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                    .subscribeSms("Yes")
                    .mobile("0777")
                    .subscribeEmail("Yes")
                    .wantSmsNotifications("Yes")
                    .email("harry.potter@mail.com")
                    .build())
                .appellantSubscription(Subscription.builder()
                    .email("appellant.old@email.com")
                    .subscribeEmail("Yes")
                    .wantSmsNotifications("Yes")
                    .subscribeSms("Yes")
                    .mobile("0777")
                    .build())
                .build()
            ).build();

        final Subscription expectedRepSubscription = Subscription.builder()
            .email("harry.potter@mail.com")
            .subscribeSms("Yes")
            .wantSmsNotifications("Yes")
            .mobile("0777")
            .subscribeEmail("Yes")
            .build();

        final Subscription expectedAppointeeSubscription = Subscription.builder()
            .email("appellant.old@email.com")
            .subscribeSms("Yes")
            .wantSmsNotifications("Yes")
            .mobile("0777")
            .subscribeEmail("Yes")
            .build();

        boolean isUpdated = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCase);
        assertTrue(isUpdated);
        assertEquals(name, existingCase.getAppeal().getAppellant().getName());
        assertEquals(expectedRepSubscription, existingCase.getSubscriptions().getRepresentativeSubscription());
        assertEquals(expectedAppointeeSubscription, existingCase.getSubscriptions().getAppellantSubscription());
        assertNull(existingCase.getSubscriptions().getAppointeeSubscription());
    }

    @Test
    public void givenAppellantUpdatesInGapsData_shouldNotOverwriteSubscriptions() throws Exception {
        gapsCaseData = CcdCasesSenderTest.buildTestCaseDataWithEventAndEvidence();
        Subscriptions subscription = Subscriptions.builder()
            .appellantSubscription(Subscription.builder()
                .tya("001")
                .build())
            .build();
        gapsCaseData.setSubscriptions(subscription);
        gapsCaseData.setAppeal(CcdCasesSenderTest.buildAppeal());

        existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_SUBSCRIPTIONS_JSON);

        updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());


        Subscriptions subscriptions = existingCaseDetails.getData().getSubscriptions();

        assertNotNull(subscriptions);

        assertThat(subscriptions.getAppellantSubscription().getTya(), equalTo("abcde12345"));
    }

    @Test
    @Parameters(method = "generateScenariosWhenExistingCcdAppellantIsNullOrEmpty")
    public void givenWeHaveToUpdateTheExistingCcdAppealDataAndGivenTheExistingAppellantIsNullOrEmpty_shouldUpdate(
        SscsCaseData existingCcdCaseData) {
        GapsAppellantData gapsAppellantData = new UpdateCcdAppellantDataTestHelper.GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S");

        Appellant appellant = Appellant.builder()
            .name(Name.builder()
                .firstName(gapsAppellantData.firstName)
                .lastName(gapsAppellantData.lastName)
                .title("Mr")
                .build())
            .contact(Contact.builder()
                .email(gapsAppellantData.email)
                .build())
            .identity(Identity.builder()
                .nino(gapsAppellantData.nino)
                .build())
            .build();

        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCcdCaseData);

        assertTrue(updateData);
        assertNull(existingCcdCaseData.getAppeal().getAppellant().getName());
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getContact().getEmail(),
            equalTo(gapsAppellantData.email));
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(gapsAppellantData.nino));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateScenariosWhenExistingCcdAppellantIsNullOrEmpty() throws IOException {
        SscsCaseDetails existingCaseDetailsWithNullFields = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetailsWithNullFields.getData().getAppeal().setAppellant(null);

        SscsCaseDetails existingCaseDetailsWithEmptyFields = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetailsWithEmptyFields.getData().getAppeal().setAppellant(Appellant.builder().build());

        return new Object[] {
            new Object[] {existingCaseDetailsWithEmptyFields.getData()},
            new Object[] {existingCaseDetailsWithNullFields.getData()}
        };
    }


    @Test
    @Parameters(method = "generateScenariosWhenGapsAppellantIsNullOrEmpty")
    public void givenWeHaveToUpdateTheExistingCcdAppealDataAndGivenTheGapsAppellantIsNullOrEmpty_shouldNotUpdate(
        SscsCaseData gapsCaseData) throws IOException {

        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        UpdateCcdAppellantDataTestHelper.ExistingCcdAppellantData existingCcdAppellantData =
            new UpdateCcdAppellantDataTestHelper.ExistingCcdAppellantData(
                "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
                "CA 36 98 74 A");
        existingCaseDetails.getData().getAppeal().getAppellant().getContact()
            .setEmail(existingCcdAppellantData.email);
        existingCaseDetails.getData().getAppeal().getAppellant().getName()
            .setFirstName(existingCcdAppellantData.firstName);
        existingCaseDetails.getData().getAppeal().getAppellant().getName()
            .setLastName(existingCcdAppellantData.lastName);
        existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().setNino(existingCcdAppellantData.nino);

        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertFalse(updateData);
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
            equalTo(existingCcdAppellantData.firstName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
            equalTo(existingCcdAppellantData.lastName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
            equalTo(existingCcdAppellantData.email));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(existingCcdAppellantData.nino));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateScenariosWhenGapsAppellantIsNullOrEmpty() {
        SscsCaseData gapsCaseDataWithNullAppellant = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(null)
                .build())
            .build();

        SscsCaseData gapsCaseDataWithEmptyAppellant = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(Appellant.builder().build())
                .build())
            .build();

        return new Object[] {
            new Object[] {gapsCaseDataWithNullAppellant},
            new Object[] {gapsCaseDataWithEmptyAppellant}
        };
    }

    @Test
    public void givenWeHaveToUpdateTheExistingCcdAppealDataAndGivenTheExistingAppellantIsNull_shouldUpdate() {
        GapsAppellantData gapsAppellantData = new UpdateCcdAppellantDataTestHelper.GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S");

        Appellant appellant = Appellant.builder()
            .name(Name.builder()
                .firstName(gapsAppellantData.firstName)
                .lastName(gapsAppellantData.lastName)
                .title("Mr")
                .build())
            .contact(Contact.builder()
                .email(gapsAppellantData.email)
                .build())
            .identity(Identity.builder()
                .nino(gapsAppellantData.nino)
                .build())
            .appointee(Appointee.builder()
                .name(Name.builder().firstName("Ap").lastName("pointee").build())
                .contact(Contact.builder().email("test@test.com").build())
                .build())
            .isAppointee("No")
            .isAddressSameAsAppointee("Yes")
            .confidentialityRequired(YesNo.NO)
            .role(Role.builder().build())
            .build();

        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();
        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(null)
                .build())
            .build();
        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCcdCaseData);

        assertTrue(updateData);
        assertNull(existingCcdCaseData.getAppeal().getAppellant().getName());
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getContact().getEmail(),
            equalTo(gapsAppellantData.email));
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(gapsAppellantData.nino));
        assertNotNull(existingCcdCaseData.getAppeal().getAppellant().getAppointee());
        assertNotNull(existingCcdCaseData.getAppeal().getAppellant().getAppointee().getContact());
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getAppointee().getContact().getEmail(),
            equalTo("test@test.com"));
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getIsAppointee(),
            equalTo("No"));
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getIsAddressSameAsAppointee(),
            equalTo("Yes"));
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getConfidentialityRequired(),
            equalTo(YesNo.NO));
        assertNotNull(existingCcdCaseData.getAppeal().getAppellant().getRole());
    }

    @Test
    public void shouldUpdateAppointeeWhenThereIsNoExistingAppointee() throws Exception {
        Appellant appellant = Appellant.builder()
            .name(Name.builder().build())
            .contact(Contact.builder().build())
            .identity(Identity.builder()
                .nino(normalisedNino)
                .build())
            .isAppointee("Yes")
            .appointee(Appointee.builder()
                .name(Name.builder().firstName("F").lastName("L").build())
                .contact(Contact.builder().build())
                .identity(Identity.builder().build())
                .address(Address.builder().build())
                .build())
            .build();
        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        gapsCaseData.getAppeal().setAppellant(appellant);
        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertTrue(updateData);
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIsAppointee(), equalTo("Yes"));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getAppointee().getContact(),
            equalTo(appellant.getAppointee().getContact()));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getAppointee().getIdentity(),
            equalTo(appellant.getAppointee().getIdentity()));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getAppointee().getAddress(),
            equalTo(appellant.getAppointee().getAddress()));
    }

    @Test
    public void shouldNotpdateAppointeeNameWhenThereIsNoExistingAppointee() throws Exception {
        Appellant appellant = Appellant.builder()
            .name(Name.builder().build())
            .contact(Contact.builder().build())
            .identity(Identity.builder()
                .nino(normalisedNino)
                .build())
            .isAppointee("Yes")
            .appointee(Appointee.builder()
                .name(Name.builder().firstName("F").lastName("L").build())
                .contact(Contact.builder().build())
                .identity(Identity.builder().build())
                .address(Address.builder().build())
                .build())
            .build();
        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        gapsCaseData.getAppeal().setAppellant(appellant);
        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetails.getData().getAppeal().getAppellant().setAppointee(
            Appointee.builder()
                .name(Name.builder().firstName("").lastName("").build()).build());
        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertTrue(updateData);
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIsAppointee(), equalTo("Yes"));
        assertEquals("",
            existingCaseDetails.getData().getAppeal().getAppellant().getAppointee().getName().getFirstName());
        assertEquals("",
            existingCaseDetails.getData().getAppeal().getAppellant().getAppointee().getName().getLastName());
    }

    @Test
    public void givenAppellantNinoUpdatesInGapsData_shouldCompareExistingCcdNinoAndReturnUpdateTrue() throws Exception {
        Appellant appellant = Appellant.builder()
                .name(Name.builder().build())
                .contact(Contact.builder().build())
                .identity(Identity.builder()
                        .nino(normalisedNino)
                        .build())
                .build();
        gapsCaseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .appellant(appellant)
                        .build())
                .build();

        gapsCaseData.getAppeal().setAppellant(appellant);

        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setFirstName("");
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setLastName("");
        existingCaseDetails.getData().getAppeal().getAppellant().getContact().setEmail("");
        String deNormalisedNino = "AB 55 44 66 B";
        existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().setNino(deNormalisedNino);

        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertTrue(updateData);

        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
                equalTo(""));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
                equalTo(""));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
                equalTo(""));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
                equalTo(normalisedNino));
    }

    @Test
    public void givenAppellantNinoUpdatesInGapsData_shouldCompareExistingCcdNinoAndGenratedNionThenReturnUpdateFalse()
            throws Exception {
        Appellant appellant = Appellant.builder()
                .name(Name.builder().build())
                .contact(Contact.builder().build())
                .identity(Identity.builder()
                        .nino(normalisedNino)
                        .build())
                .build();
        gapsCaseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .appellant(appellant)
                        .build())
                .build();

        gapsCaseData.getAppeal().setAppellant(appellant);

        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setFirstName("");
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setLastName("");
        existingCaseDetails.getData().getAppeal().getAppellant().getContact().setEmail("");
        existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().setNino(normalisedNino);

        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertFalse(updateData);

        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
                equalTo(""));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
                equalTo(""));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
                equalTo(""));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
                equalTo(normalisedNino));


    }

    @Test
    @Parameters(method = "generateUpdateCaseDataScenarios")
    public void givenAppellantUpdatesInGapsData_shouldUpdateExistingCcdAppellantData(
        UpdateCcdAppellantDataTestHelper.GapsAndCcdDataUpdateScenario gapsAndCcdDataUpdateScenario,
        boolean expectedUpdateData) throws Exception {

        Appellant appellant = Appellant.builder()
            .name(Name.builder()
                .firstName(gapsAndCcdDataUpdateScenario.gapsAppellantData.firstName)
                .lastName(gapsAndCcdDataUpdateScenario.gapsAppellantData.lastName)
                .title("Mr")
                .build())
            .contact(Contact.builder()
                .email(gapsAndCcdDataUpdateScenario.gapsAppellantData.email)
                .mobile(gapsAndCcdDataUpdateScenario.gapsAppellantData.mobile)
                .phone(gapsAndCcdDataUpdateScenario.gapsAppellantData.phone)
                .build())
            .identity(Identity.builder()
                .nino(gapsAndCcdDataUpdateScenario.gapsAppellantData.nino)
                .build())
            .appointee(gapsAndCcdDataUpdateScenario.gapsAppellantData.appointee)
            .build();

        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        gapsCaseData.getAppeal().setAppellant(appellant);

        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetails.getData().getAppeal().getAppellant().getContact().setEmail(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.email);
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setFirstName(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.firstName);
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setLastName(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.lastName);
        existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().setNino(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.nino);
        existingCaseDetails.getData().getAppeal().getAppellant().setAppointee(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.appointee);

        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertEquals(expectedUpdateData, updateData);
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.firstName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.lastName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.email));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.nino));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getAppointee(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.appointee));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateUpdateCaseDataScenarios() {
        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths =
            updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithEmptyFields =
            updateCcdDataWhenThereAreGapsDataUpdatesWithEmptyFields();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithNullFields =
            updateCcdDataWhenThereAreGapsDataUpdatesWithNullFields();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreExistingCcdDataUpdatesWithEmptyFields =
            updateCcdDataWhenThereAreExistingCcdDataUpdatesWithEmptyFields();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreExistingCcdDataUpdatesWithNullFields =
            updateCcdDataWhenThereAreExistingCcdDataUpdatesWithNullFields();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeHappyPaths =
            updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeContactHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithNewAppointeeContactHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeNameHappyPaths =
            updateCcdDataNoWhenThereAreGapsDataWithNewAppointeeNameHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeIdentityHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithNewAppointeeIdentityHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedMobileHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithUpdatedMobileHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedPhoneHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithUpdatedPhoneHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeNinoHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeNinoHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeEmailHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeEmailHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeMobileHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeMobileHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeePhoneHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeePhoneHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeFirstNameHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeFirstNameHappyPaths();

        GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeLastNameHappyPaths =
            updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeLastNameHappyPaths();

        return new Object[] {
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithEmptyFields, false},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithNullFields, false},
            new Object[] {updateCcdDataWhenThereAreExistingCcdDataUpdatesWithEmptyFields, true},
            new Object[] {updateCcdDataWhenThereAreExistingCcdDataUpdatesWithNullFields, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeContactHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeNameHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeIdentityHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataWithUpdatedMobileHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataWithUpdatedPhoneHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeNinoHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeEmailHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeMobileHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeePhoneHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeFirstNameHappyPaths, true},
            new Object[] {updateCcdDataWhenThereAreGapsDataUpdatesWithUpdatedAppointeeLastNameHappyPaths, true}
        };
    }
}
