package uk.gov.hmcts.reform.sscs.transform;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import uk.gov.hmcts.reform.sscs.models.Appeal;
import uk.gov.hmcts.reform.sscs.models.Appellant;
import uk.gov.hmcts.reform.sscs.models.CcdCase;
import uk.gov.hmcts.reform.sscs.models.Identity;
import uk.gov.hmcts.reform.sscs.models.Name;
import uk.gov.hmcts.reform.sscs.models.gaps2.Gaps2Extract;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AppealCaseToCcdCaseTransformerTest {
    
    private static final String DELTA_JSON = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";
    
    @Test
    public void shouldTransformAppealCase() throws IOException {

        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true).build();

        String jsonExtract = FileUtils.getFileContentGivenFilePath(DELTA_JSON);

        Gaps2Extract gaps2Extract = mapper.readerFor(Gaps2Extract.class).readValue(jsonExtract);

        CcdCase actualCcdCase = new AppealCaseToCcdCaseTransformer()
                .transform(gaps2Extract.getAppealCases().getAppealCaseList().get(0));

        assertEquals(buildCcdCaseData(), actualCcdCase);
    }

    private CcdCase buildCcdCaseData() {

        Name name = Name.builder()
                .title("Mrs.")
                .firstName("E")
                .lastName("Elderberry")
                .build();
        Identity identity = Identity.builder()
                .nino("AB 22 55 66 B")
                .build();
        Appellant appellant = Appellant.builder()
                .name(name)
                .identity(identity)
                .build();
        Appeal appeal = Appeal.builder()
                .appellant(appellant)
                .build();

        return CcdCase.builder()
                .caseReference("SC068/17/00013")
                .appeal(appeal)
                .build();
    }

}

