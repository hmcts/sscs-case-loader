package uk.gov.hmcts.reform.sscs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;

public final class CaseDetailsUtils {

    private CaseDetailsUtils() {
    }

    public static CaseDetails getCaseDetails(String caseDetails) throws IOException {
        InputStream resourceAsStream = CaseDetailsUtils.class.getClassLoader().getResourceAsStream(caseDetails);
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        return mapper.readerFor(CaseDetails.class).readValue(resourceAsStream);
    }


    public static SscsCaseDetails getSscsCaseDetails(String caseDetails) throws IOException {
        SscsCcdConvertService sscsCcdConvertService = new SscsCcdConvertService();
        InputStream resourceAsStream = CaseDetailsUtils.class.getClassLoader().getResourceAsStream(caseDetails);
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        return sscsCcdConvertService.getCaseDetails((CaseDetails) mapper.readerFor(CaseDetails.class).readValue(resourceAsStream));
    }

}
