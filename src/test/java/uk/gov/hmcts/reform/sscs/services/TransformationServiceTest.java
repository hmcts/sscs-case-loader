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
    private TransformAppealCaseToCaseData transform;

    private InputStream is;
    private CaseData caseData;

    @Before
    public void setUp() {
        is = getClass().getClassLoader().getResourceAsStream("process_case_test_delta.xml");
        caseData = CaseData.builder().build();
        when(transform.transform(any())).thenReturn(caseData);
    }

    @Test
    public void shouldReturnListOfCasesGivenDeltaAsInputStream() {
        TransformationService service = new TransformationService(transform, "2017-01-01");
        List<CaseData> caseDataList = service.transform(is);
        assertThat(caseDataList.size(), is(1));
        assertThat(caseDataList.get(0), is(caseData));
    }
}
