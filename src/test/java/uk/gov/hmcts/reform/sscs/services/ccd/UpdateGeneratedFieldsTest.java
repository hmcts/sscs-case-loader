package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@RunWith(JUnitParamsRunner.class)
public class UpdateGeneratedFieldsTest {

    private final UpdateGeneratedFields updateGeneratedFields = new UpdateGeneratedFields();

    @Test
    @Parameters(method = "generateInvalidScenarios")
    public void givenInvalidExistingCcdCaseData_shouldNotGenerateFields(SscsCaseData existingCcdCaseData,
                                                                        SscsCaseData expectedExistingCcdCaseData) {
        updateGeneratedFields.updateGeneratedFields(existingCcdCaseData);
        assertEquals(expectedExistingCcdCaseData, existingCcdCaseData);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateInvalidScenarios() {
        SscsCaseData existingCaseDataWithNullAppeal = SscsCaseData.builder()
            .build();
        SscsCaseData existingCaseDataWithNullAppellant = SscsCaseData.builder()
            .appeal(Appeal.builder().build())
            .build();
        return new Object[]{
            new Object[]{null, null},
            new Object[]{existingCaseDataWithNullAppeal, existingCaseDataWithNullAppeal},
            new Object[]{existingCaseDataWithNullAppellant, existingCaseDataWithNullAppellant}
        };
    }

    @Test
    @Parameters(method = "generateAppellantNameScenarios")
    public void givenValidData_shouldUpdateAppellantName(Appellant appellant, String expectedLastName) {
        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        updateGeneratedFields.updateGeneratedFields(existingCcdCaseData);

        assertThat(existingCcdCaseData.getGeneratedSurname(), equalTo(expectedLastName));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateAppellantNameScenarios() {
        Appellant appellantWithLastName = Appellant.builder()
            .name(Name.builder()
                .lastName("lastName")
                .build())
            .build();

        Appellant appellantWithNullLastName = Appellant.builder()
            .name(Name.builder()
                .build())
            .build();

        Appellant appellantWithNullName = Appellant.builder().build();

        Appellant appellantWithEmptylLastName = Appellant.builder()
            .name(Name.builder()
                .lastName("")
                .build())
            .build();

        return new Object[]{
            new Object[]{appellantWithLastName, "lastName"},
            new Object[]{appellantWithNullLastName, null},
            new Object[]{appellantWithEmptylLastName, null},
            new Object[]{appellantWithNullName, null}
        };
    }

    @Test
    @Parameters(method = "generateAppellantIdentityScenarios")
    public void givenValidData_shouldUpdateAppellantIdentity(Appellant appellant, String expectedDob,
                                                             String expectedNino) {
        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        updateGeneratedFields.updateGeneratedFields(existingCcdCaseData);

        assertThat(existingCcdCaseData.getGeneratedDob(), equalTo(expectedDob));
        assertThat(existingCcdCaseData.getGeneratedNino(), equalTo(expectedNino));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateAppellantIdentityScenarios() {
        Appellant appellantWithNullIdentity = Appellant.builder().build();

        Appellant appellantWithNullDobAndNinoIdentity = Appellant.builder()
            .identity(Identity.builder().build())
            .build();

        Appellant appellantWithEmptyDobAndNinoIdentity = Appellant.builder()
            .identity(Identity.builder()
                .dob("")
                .nino("")
                .build())
            .build();

        Appellant appellantWithIdentity = Appellant.builder()
            .identity(Identity.builder()
                .dob("1982-12-10")
                .nino("NW 23 34 45 A")
                .build())
            .build();

        return new Object[]{
            new Object[]{appellantWithNullIdentity, null, null},
            new Object[]{appellantWithNullDobAndNinoIdentity, null, null},
            new Object[]{appellantWithEmptyDobAndNinoIdentity, null, null},
            new Object[]{appellantWithIdentity, "1982-12-10", "NW 23 34 45 A"}
        };
    }

}
