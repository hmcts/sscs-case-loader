package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;

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

    //TODO scenario when the existing ccd is null or empty

    @Test
    @Parameters(method = "generateScenariosWhenGapsAppellantIsNullOrEmpty")
    public void givenWeHaveToUpdateTheExistingCcdAppealDataAndGivenTheGapsAppellantIsNullOrEmpty_shouldNotUpdate(
        SscsCaseData gapsCaseData) throws IOException {

        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
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

        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
            equalTo(existingCcdAppellantData.firstName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
            equalTo(existingCcdAppellantData.lastName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
            equalTo(existingCcdAppellantData.contactEmail));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(existingCcdAppellantData.nino));
        assertFalse(updateData);
    }

    public Object[] generateScenariosWhenGapsAppellantIsNullOrEmpty() {
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
        GapsAndCcdDataUpdateScenario gapsAndCcdDataUpdateScenario, boolean expectedUpdateData) throws Exception {

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

        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.firstName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.lastName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.contactEmail));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(gapsAndCcdDataUpdateScenario.expectedExistingCcdAppellantName.nino));
        assertEquals(expectedUpdateData, updateData);

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

    private GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    private GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithEmptyFields() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "", "", "", "");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("existingFirstName", "existingLastName",
                "existingCaseEmail@email.com", "CA 36 98 74 A");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    private GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithNullFields() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            null, null, null, null);

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("existingFirstName", "existingLastName",
                "existingCaseEmail@email.com", "CA 36 98 74 A");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    private GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreExistingCcdDataUpdatesWithEmptyFields() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "", "", "", "");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    private GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreExistingCcdDataUpdatesWithNullFields() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "null", "null", "null", "null");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }


    private class GapsAppellantData {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;

        GapsAppellantData(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }
    }

    private class ExpectedExistingCcdAppellantName {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;

        ExpectedExistingCcdAppellantName(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }
    }

    private class ExistingCcdAppellantData {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;

        ExistingCcdAppellantData(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }
    }

    private class GapsAndCcdDataUpdateScenario {
        GapsAppellantData gapsAppellantData;
        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName;
        ExistingCcdAppellantData existingCcdAppellantData;

        GapsAndCcdDataUpdateScenario(GapsAppellantData gapsAppellantData,
                                     ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName,
                                     ExistingCcdAppellantData existingCcdAppellantData) {
            this.gapsAppellantData = gapsAppellantData;
            this.expectedExistingCcdAppellantName = expectedExistingCcdAppellantName;
            this.existingCcdAppellantData = existingCcdAppellantData;
        }
    }
}
