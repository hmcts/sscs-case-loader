package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
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
    private static final String NINO = "AB46575S";

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
    @Parameters({
        "first-name,first-name,last-name,last-name,email@email.com,email@email.com,AB46575S,AB46575S",
        ",A,,Cherry,,existingCaseEmail@email.com,,CA 36 98 74 A",
        "null,A,null,Cherry,null,existingCaseEmail@email.com,null,CA 36 98 74 A"
    })
    public void givenAppellantUpdatesInGapsData_shouldUpdateExistingCcdAppellantData(
        @Nullable String firstName, String expectedFirstName,
        @Nullable String lastName, String expectedLastName,
        @Nullable String contactEmail, @Nullable String expectedContactEmail,
        @Nullable String nino, String expectedNino) throws Exception {

        Appellant appellant = Appellant.builder()
            .name(Name.builder()
                .firstName(firstName)
                .lastName(lastName)
                .title("Mr")
                .build())
            .contact(Contact.builder()
                .email(contactEmail)
                .build())
            .identity(Identity.builder()
                .nino(nino)
                .build())
            .build();

        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        gapsCaseData.getAppeal().setAppellant(appellant);

        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetails.getData().getAppeal().getAppellant().getContact().setEmail("existingCaseEmail@email.com");

        updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
            equalTo(expectedFirstName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
            equalTo(expectedLastName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
            equalTo(expectedContactEmail));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(expectedNino));
    }

}
