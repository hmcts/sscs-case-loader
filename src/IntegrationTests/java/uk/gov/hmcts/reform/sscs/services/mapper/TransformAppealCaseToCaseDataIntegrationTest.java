package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransformAppealCaseToCaseDataIntegrationTest {

    @MockBean
    private ReferenceDataService referenceDataService;

    @Autowired
    private TransformAppealCaseToCaseData transformAppealCaseToCaseData;

    @Test
    public void givenHearingAdjournedEvent_shouldSetAdjournedFlagToYes() throws Exception {
        final AppealCase appealCase = getAppealCase("AppealCaseWithAdjournedEvent.json");

        when(referenceDataService.getBenefitType(anyString())).thenReturn("pip");
        when(referenceDataService.getVenueDetails(anyString())).thenReturn(VenueDetails.builder().build());

        final SscsCaseData caseData = transformAppealCaseToCaseData.transform(appealCase);

        assertThat(caseData.getHearings().size(), is(1));
        assertThat(caseData.getHearings().get(0).getValue().getAdjourned(), is("Yes"));
    }

    private AppealCase getAppealCase(String filename) throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder
            .json()
            .indentOutput(true)
            .build();

        String appealCaseJson = IOUtils.toString(Objects.requireNonNull(TransformAppealCaseToCaseDataTest.class
            .getClassLoader().getResourceAsStream(filename)), StandardCharsets.UTF_8.name()
        );

        return mapper.readerFor(AppealCase.class).readValue(appealCaseJson);
    }

}
