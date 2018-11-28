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
        "first-name, first-name, last-name, last-name, email@email.com, email@email.com, AB46575S, AB46575S,"
            + "existingCaseEmail@email.com, existingFirstName, existingLastName, CA 36 98 74 A",
        ", existingFirstName,, existingLastName,, existingCaseEmail@email.com,, CA 36 98 74 A, "
            + "existingCaseEmail@email.com, existingFirstName, existingLastName, CA 36 98 74 A",
        "null, existingFirstName, null, existingLastName, null, existingCaseEmail@email.com, null, CA 36 98 74 A, "
            + "existingCaseEmail@email.com, existingFirstName, existingLastName, CA 36 98 74 A",
        "null, existingFirstName, null, existingLastName, email@email.com, email@email.com, null, CA 36 98 74 A, null,"
            + "existingFirstName, existingLastName, CA 36 98 74 A",
        "null, existingFirstName, null, existingLastName, email@email.com, email@email.com, null, CA 36 98 74 A,,"
            + "existingFirstName, existingLastName, CA 36 98 74 A",
        "null, existingFirstName, null, existingLastName,,null, null, CA 36 98 74 A, null, existingFirstName, "
            + "existingLastName, CA 36 98 74 A",
        "first-name, first-name, null, existingLastName,,null, null, CA 36 98 74 A, null,, existingLastName, "
            + "CA 36 98 74 A",
        "first-name, first-name, null, existingLastName,,null, null, CA 36 98 74 A, null, null, existingLastName, "
            + "CA 36 98 74 A",
        "first-name, first-name, null, existingLastName,,null,,, null, null, existingLastName,",
        "first-name, first-name, null, existingLastName,,null,null,null, null, null, existingLastName, null"
    })
    public void givenAppellantUpdatesInGapsData_shouldUpdateExistingCcdAppellantData(
        @Nullable String gapsFirstName, @Nullable String expectedExistingCcdFirstName,
        @Nullable String gapsLastName, @Nullable String expectedExistingCcdLastName,
        @Nullable String gapsContactEmail, @Nullable String expectedExistingCcdContactEmail,
        @Nullable String gapsNino, @Nullable String expectedExistingCcdNino,
        @Nullable String existingCcdContactEmail, @Nullable String existingCddFirstName,
        @Nullable String existingCcdLastName, @Nullable String existingCcdNino) throws Exception {

        Appellant appellant = Appellant.builder()
            .name(Name.builder()
                .firstName(gapsFirstName)
                .lastName(gapsLastName)
                .title("Mr")
                .build())
            .contact(Contact.builder()
                .email(gapsContactEmail)
                .build())
            .identity(Identity.builder()
                .nino(gapsNino)
                .build())
            .build();

        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        gapsCaseData.getAppeal().setAppellant(appellant);

        existingCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);
        existingCaseDetails.getData().getAppeal().getAppellant().getContact().setEmail(existingCcdContactEmail);
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setFirstName(existingCddFirstName);
        existingCaseDetails.getData().getAppeal().getAppellant().getName().setLastName(existingCcdLastName);
        existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().setNino(existingCcdNino);

        updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData());

        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(),
            equalTo(expectedExistingCcdFirstName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(),
            equalTo(expectedExistingCcdLastName));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
            equalTo(expectedExistingCcdContactEmail));
        assertThat(existingCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(),
            equalTo(expectedExistingCcdNino));
    }

}
