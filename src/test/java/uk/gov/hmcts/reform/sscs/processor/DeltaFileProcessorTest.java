package uk.gov.hmcts.reform.sscs.processor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import uk.gov.hmcts.reform.sscs.models.gaps2.Gaps2Extract;
import uk.gov.hmcts.reform.sscs.transform.AppealCaseToCcdCaseTransformer;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import java.io.IOException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeltaFileProcessorTest {
    
    private static final String DELTA_JSON = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";
    
    private DeltaFileProcessor deltaFileProcessor;
    
    @Mock
    private  AppealCaseToCcdCaseTransformer appealCaseToCcdCaseTransformer;
    
    @Before
    public void setUp() {
        deltaFileProcessor = new DeltaFileProcessor(appealCaseToCcdCaseTransformer);
    }
    
    @Test
    public void shouldProcess() throws IOException {
        
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true).build();

        String jsonExtract = FileUtils.getFileContentGivenFilePath(DELTA_JSON);

        Gaps2Extract gaps2Extract = mapper.readerFor(Gaps2Extract.class).readValue(jsonExtract);

        deltaFileProcessor.process(jsonExtract);
        
        verify(appealCaseToCcdCaseTransformer).transform(eq(gaps2Extract.getAppealCases().getAppealCaseList().get(0)));

    }


}

