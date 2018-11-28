package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;

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

    @Test
//    @Parameters({
//        "first-name, first-name, last-name, last-name, email@email.com, email@email.com, AB46575S, AB46575S,"
//            + "existingCaseEmail@email.com, existingFirstName, existingLastName, CA 36 98 74 A",
//        ", existingFirstName,, existingLastName,, existingCaseEmail@email.com,, CA 36 98 74 A, "
//            + "existingCaseEmail@email.com, existingFirstName, existingLastName, CA 36 98 74 A",
//        "null, existingFirstName, null, existingLastName, null, existingCaseEmail@email.com, null, CA 36 98 74 A, "
//            + "existingCaseEmail@email.com, existingFirstName, existingLastName, CA 36 98 74 A",
//        "null, existingFirstName, null, existingLastName, email@email.com, email@email.com, null, CA 36 98 74 A, null,"
//            + "existingFirstName, existingLastName, CA 36 98 74 A",
//        "null, existingFirstName, null, existingLastName, email@email.com, email@email.com, null, CA 36 98 74 A,,"
//            + "existingFirstName, existingLastName, CA 36 98 74 A",
//        "null, existingFirstName, null, existingLastName,,null, null, CA 36 98 74 A, null, existingFirstName, "
//            + "existingLastName, CA 36 98 74 A",
//        "first-name, first-name, null, existingLastName,,null, null, CA 36 98 74 A, null,, existingLastName, "
//            + "CA 36 98 74 A",
//        "first-name, first-name, null, existingLastName,,null, null, CA 36 98 74 A, null, null, existingLastName, "
//            + "CA 36 98 74 A",
//        "first-name, first-name, null, existingLastName,,null,,, null, null, existingLastName,",
//        "first-name, first-name, null, existingLastName,,null,null,null, null, null, existingLastName, null"
//    })
    @Parameters(method = "generateUpdateCaseDataScenarios")
    public void givenAppellantUpdatesInGapsData_shouldUpdateExistingCcdAppellantData(
        GapsAndCcdDataUpdateScenario gapsAndCcdDataUpdateScenario) throws Exception {

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

        updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

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
        GapsAndCcdDataUpdateScenario gapsAndCcdDataUpdateScenario =
            updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths();

        return new Object[]{
            new Object[]{gapsAndCcdDataUpdateScenario}
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

    private class GapsAppellantData {
        private final String firstName;
        private final String lastName;
        private final String contactEmail;
        private final String nino;

        private GapsAppellantData(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }
    }

    private class ExpectedExistingCcdAppellantName {
        private final String firstName;
        private final String lastName;
        private final String contactEmail;
        private final String nino;

        private ExpectedExistingCcdAppellantName(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }
    }

    private class ExistingCcdAppellantData {
        private final String firstName;
        private final String lastName;
        private final String contactEmail;
        private final String nino;

        private ExistingCcdAppellantData(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }
    }

    private class GapsAndCcdDataUpdateScenario {
        private final GapsAppellantData gapsAppellantData;
        private final ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName;
        private final ExistingCcdAppellantData existingCcdAppellantData;

        private GapsAndCcdDataUpdateScenario(GapsAppellantData gapsAppellantData,
                                             ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName,
                                             ExistingCcdAppellantData existingCcdAppellantData) {
            this.gapsAppellantData = gapsAppellantData;
            this.expectedExistingCcdAppellantName = expectedExistingCcdAppellantName;
            this.existingCcdAppellantData = existingCcdAppellantData;
        }
    }
}
