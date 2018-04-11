package uk.gov.hmcts.reform.sscs.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sscs.exceptions.JsonMapperErrorException;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;

public final class CcdUtil {

    private CcdUtil() {

    }

    public static CaseData getCaseData(Object object) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        try {
            return mapper.convertValue(object, CaseData.class);
        } catch (Exception e) {
            throw new JsonMapperErrorException("Error occurred when CaseDetails are mapped into CaseData", e);
        }
    }
}
