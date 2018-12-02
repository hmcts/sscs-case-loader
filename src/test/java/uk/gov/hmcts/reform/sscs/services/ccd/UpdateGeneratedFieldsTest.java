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
    @Parameters(method = "generateAppellantScenarios")
    public void givenValidData_shouldUpdateAppellantName(Appellant appellant) {
        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(appellant)
                .build())
            .build();

        updateGeneratedFields.updateGeneratedFields(existingCcdCaseData);

        String expectedLastName = existingCcdCaseData.getAppeal().getAppellant().getName().getLastName();
        assertThat(existingCcdCaseData.getGeneratedSurname(), equalTo(expectedLastName));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateAppellantScenarios() {
        Appellant appellant = Appellant.builder()
            .name(Name.builder()
                .lastName("lastName")
                .build())
            .build();
        return new Object[]{
            new Object[]{appellant}
        };
    }


}
