package uk.gov.hmcts.reform.sscs.services;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformAppealCaseToCaseData;

@RunWith(MockitoJUnitRunner.class)
public class TransformationServiceTest {

    @Mock
    private TransformAppealCaseToCaseData transformAppealCaseToCaseData;
    private CaseData caseData;
    private TransformationService transformationService;
    private InputStream is;
    private List<CaseData> caseDataList;

    @Before
    public void setUp() {
        transformationService = new TransformationService(transformAppealCaseToCaseData,
            "2017-01-01");
        caseData = CaseData.builder().build();
        when(transformAppealCaseToCaseData.transform(any())).thenReturn(caseData);
    }

    @Test
    public void shouldReturnListOfCasesGivenDeltaAsInputStream() {
        is = getClass().getClassLoader().getResourceAsStream("process_case_test_delta.xml");
        caseDataList = transformationService.transform(is);
        assertThat(caseDataList.size(), is(1));
        assertThat(caseDataList.get(0), is(caseData));
    }

    @Test
    public void givenDeltaWithNoCases_shouldReturnZeroCases() {
        is = getClass().getClassLoader().getResourceAsStream("delta_with_no_cases.xml");
        caseDataList = transformationService.transform(is);
        assertThat(caseDataList.size(), is(0));
    }
}
