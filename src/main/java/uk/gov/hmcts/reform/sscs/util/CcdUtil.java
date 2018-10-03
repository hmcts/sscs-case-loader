//package uk.gov.hmcts.reform.sscs.util;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
//import uk.gov.hmcts.reform.sscs.exceptions.JsonMapperErrorException;
//
//public final class CcdUtil {
//
//    private CcdUtil() {
//
//    }
//
//    public static SscsCaseData getCaseData(Object object) {
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
//
//        try {
//            return mapper.convertValue(object, SscsCaseData.class);
//        } catch (Exception e) {
//            throw new JsonMapperErrorException("Error occurred when CaseDetails are mapped into CaseData", e);
//        }
//    }
//}
