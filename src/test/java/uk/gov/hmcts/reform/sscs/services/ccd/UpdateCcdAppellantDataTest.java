package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreExistingCcdDataUpdatesWithEmptyFields;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreExistingCcdDataUpdatesWithNullFields;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataUpdatesWithEmptyFields;
import static uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.updateCcdDataWhenThereAreGapsDataUpdatesWithNullFields;

import java.io.IOException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Contact;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.GapsAndCcdDataUpdateScenario;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCcdAppellantDataTestHelper.GapsAppellantData;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdAppellantDataTest {

    private static final String CASE_DETAILS_WITH_SUBSCRIPTIONS_JSON = "CaseDetailsWithSubscriptions.json";

    private final UpdateCcdAppellantData updateCcdAppellantData = new UpdateCcdAppellantData();
    private SscsCaseData gapsCaseData;
    private SscsCaseDetails existingCaseDetails;

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
                .email(gapsAppellantData.contactEmail)
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
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getName().getFirstName(),
            equalTo(gapsAppellantData.firstName));
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getName().getLastName(),
            equalTo(gapsAppellantData.lastName));
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getContact().getEmail(),
            equalTo(gapsAppellantData.contactEmail));
        assertThat(existingCcdCaseData.getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(gapsAppellantData.nino));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateScenariosWhenExistingCcdAppellantIsNullOrEmpty() throws IOException {
        SscsCaseDetails existingCaseDetailsWithNullFields = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetailsWithNullFields.getData().getAppeal().setAppellant(null);

        SscsCaseDetails existingCaseDetailsWithEmptyFields = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetailsWithEmptyFields.getData().getAppeal().setAppellant(Appellant.builder().build());

        return new Object[]{
            new Object[]{existingCaseDetailsWithEmptyFields.getData()},
            new Object[]{existingCaseDetailsWithNullFields.getData()}
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
            .setEmail(existingCcdAppellantData.contactEmail);
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
            equalTo(existingCcdAppellantData.contactEmail));
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

        return new Object[]{
            new Object[]{gapsCaseDataWithNullAppellant},
            new Object[]{gapsCaseDataWithEmptyAppellant}
        };
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
                .email(gapsAndCcdDataUpdateScenario.gapsAppellantData.contactEmail)
                .build())
            .identity(Identity.builder()
                .nino(gapsAndCcdDataUpdateScenario.gapsAppellantData.nino)
                .build())
            .build();

        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        gapsCaseData.getAppeal().setAppellant(appellant);

        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetails.getData().getAppeal().getAppellant().getContact().setEmail(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.contactEmail);
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setFirstName(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.firstName);
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setLastName(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.lastName);
        existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().setNino(
            gapsAndCcdDataUpdateScenario.existingCcdAppellantData.nino);

        boolean updateData = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertEquals(expectedUpdateData, updateData);
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.firstName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.lastName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.contactEmail));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.nino));
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

        return new Object[]{
            new Object[]{updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths, true},
            new Object[]{updateCcdDataWhenThereAreGapsDataUpdatesWithEmptyFields, false},
            new Object[]{updateCcdDataWhenThereAreGapsDataUpdatesWithNullFields, false},
            new Object[]{updateCcdDataWhenThereAreExistingCcdDataUpdatesWithEmptyFields, true},
            new Object[]{updateCcdDataWhenThereAreExistingCcdDataUpdatesWithNullFields, true}
        };
    }

}
