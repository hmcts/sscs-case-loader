package uk.gov.hmcts.reform.sscs.services;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformAppealCaseToCaseData;

@RunWith(MockitoJUnitRunner.class)
public class TransformationServiceTest {

    @Mock
    private TransformAppealCaseToCaseData transformAppealCaseToCaseData;
    private SscsCaseData caseDataWithScReference;
    private SscsCaseData caseDataWithCcdId;
    private TransformationService transformationService;
    private InputStream is;
    private List<SscsCaseData> caseDataList;

    @Before
    public void setUp() {
        transformationService = new TransformationService(transformAppealCaseToCaseData,
            "2017-01-01");

        caseDataWithScReference = SscsCaseData.builder().build();
        caseDataWithScReference.setCaseReference("SC012/34/56789");
        caseDataWithScReference.setCcdCaseId(null);

        caseDataWithCcdId = SscsCaseData.builder().build();
        caseDataWithCcdId.setCaseReference(null);
        caseDataWithCcdId.setCcdCaseId("1234567890");

        when(transformAppealCaseToCaseData.transform(any()))
            .thenReturn(caseDataWithScReference)
            .thenReturn(caseDataWithCcdId);
    }

    @Test
    public void shouldReturnListOfCasesGivenDeltaAsInputStream() {
        is = getClass().getClassLoader().getResourceAsStream("process_case_test_delta.xml");
        caseDataList = transformationService.transform(is);
        assertThat(caseDataList.size(), is(2));
        assertThat(caseDataList.get(0), is(caseDataWithScReference));
        assertThat(caseDataList.get(1), is(caseDataWithCcdId));
    }

    @Test
    public void givenDeltaWithNoCases_shouldReturnZeroCases() {
        is = getClass().getClassLoader().getResourceAsStream("delta_with_no_cases.xml");
        caseDataList = transformationService.transform(is);
        assertThat(caseDataList.size(), is(0));
    }
}
