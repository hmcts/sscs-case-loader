package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;
import static uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSenderTest.buildTestCaseDataWithAppellantAndBenefitType;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Contact;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;

public class UpdateCcdAppellantDataTest {

    private static final String CASE_DETAILS_WITH_SUBSCRIPTIONS_JSON = "CaseDetailsWithSubscriptions.json";
    private static final String FIRST_NAME = "first-name";
    private static final String LAST_NAME = "last-name";
    private static final String EMAIL_EMAIL_COM = "email@email.com";
    private static final String NINO = "AB46575S";
    private static final String DOB = "12-20-2018";
    private static final String MOBILE = "07777777777";

    private final UpdateCcdAppellantData updateCcdAppellantData = new UpdateCcdAppellantData();

    @Test
    public void givenAppellantUpdatesInGapsData_shouldNotOverwriteSubscriptions() throws Exception {
        SscsCaseData caseData = CcdCasesSenderTest.buildTestCaseDataWithEventAndEvidence();
        Subscriptions subscription = Subscriptions.builder()
            .appellantSubscription(Subscription.builder()
                .tya("001")
                .build())
            .build();
        caseData.setSubscriptions(subscription);
        caseData.setAppeal(CcdCasesSenderTest.buildAppeal());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_SUBSCRIPTIONS_JSON);

        updateCcdAppellantData.updateCcdAppellantData(caseData, existingCaseDetails.getData());


        Subscriptions subscriptions = existingCaseDetails.getData().getSubscriptions();

        assertNotNull(subscriptions);

        assertThat(subscriptions.getAppellantSubscription().getTya(), equalTo("abcde12345"));

    }

    @Test
    public void givenAppellantUpdatesInGapsData_shouldUpdateExistingCcdAppellantData() throws Exception {
        SscsCaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();

        Appellant appellant = Appellant.builder()
            .name(Name.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .title("Mr")
                .build())
            .contact(Contact.builder()
                .email(EMAIL_EMAIL_COM)
                .mobile(MOBILE)
                .build())
            .identity(Identity.builder()
                .nino(NINO)
                .dob(DOB)
                .build())
            .build();

        caseData.getAppeal().setAppellant(appellant);

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CcdCasesSenderTest.CASE_DETAILS_JSON);

        updateCcdAppellantData.updateCcdAppellantData(caseData, sscsCaseDetails.getData());


        assertThat(sscsCaseDetails.getData().getAppeal().getAppellant().getName().getFirstName(), equalTo(FIRST_NAME));
        assertThat(sscsCaseDetails.getData().getAppeal().getAppellant().getName().getLastName(), equalTo(LAST_NAME));
        assertThat(sscsCaseDetails.getData().getAppeal().getAppellant().getContact().getEmail(),
            equalTo(EMAIL_EMAIL_COM));
        assertThat(sscsCaseDetails.getData().getAppeal().getAppellant().getIdentity().getNino(), equalTo(NINO));
        //        assertThat(sscsCaseData.getGeneratedSurname(), equalTo(LAST_NAME));
        //        assertThat(sscsCaseData.getGeneratedNino(), equalTo(NINO));
        //        assertThat(sscsCaseData.getGeneratedEmail(), equalTo(EMAIL_EMAIL_COM));
        //        assertThat(sscsCaseData.getGeneratedDob(), equalTo(DOB));
        //        assertThat(sscsCaseData.getGeneratedMobile(), equalTo(MOBILE));
    }


}
