package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertEquals;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@RunWith(JUnitParamsRunner.class)
public class UpdateGeneratedFieldsTest {
    @Test
    @Parameters(method = "generateInvalidScenarios")
    public void givenInvalidExistingCcdCaseData_shouldNotGenerateFields(SscsCaseData existingCcdCaseData,
                                                                        SscsCaseData expectedExistingCcdCaseData) {
        UpdateGeneratedFields updateGeneratedFields = new UpdateGeneratedFields();
        updateGeneratedFields.updateGeneratedFields(existingCcdCaseData);
        assertEquals(expectedExistingCcdCaseData, existingCcdCaseData);
    }

    public Object[] generateInvalidScenarios() {
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

}
