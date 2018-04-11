package uk.gov.hmcts.reform.sscs.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.exceptions.JsonMapperErrorException;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;


public class CcdUtilTest {


    public static final String CASE_DETAILS_WITH_HEARINGS_JSON = "CaseDetailsWithHearings.json";

    @Test
    public void shouldReturnCaseDataFromCaseDetailsObject() throws Exception {

        CaseData caseData = CcdUtil.getCaseData(getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON).getData());
        assertNotNull(caseData);
        assertThat(caseData.getCaseReference(), Matchers.equalTo("SC068/17/00011"));

    }

    @Test(expected = JsonMapperErrorException.class)
    public void shouldThrowApplicationErrorExceptionIfItsNotAValidCaseJson() throws Exception {
        CcdUtil.getCaseData(GapsEvent.APPEAL_DORMANT);
    }

    private CaseDetails getCaseDetails(String caseDetailsFileName) throws Exception {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(caseDetailsFileName);
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        return mapper.readerFor(CaseDetails.class).readValue(resourceAsStream);
    }
}
