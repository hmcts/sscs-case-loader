package uk.gov.hmcts.reform.tools.builders;

import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.reform.tools.enums.MajorStatusType;
import uk.gov.hmcts.reform.tools.utils.XMLTags;

public class AppealMajorStatus {

    private Map<String, String> appealMajorStatuses = new HashMap<>();

//    public AppealMajorStatus setMajorStatus(MajorStatusType majorStatusType, String transDate){
//        setStatusCode(majorStatusType);
//        setDateSet(transDate);
//        setBFDate(transDate);
//        setDateClosed(transDate);
//        return this;
//    }

    public AppealMajorStatus setStatusCode(MajorStatusType majorStatusType) {
        appealMajorStatuses.put(XMLTags.status_Id, majorStatusType.getCode());
        return this;
    }


    public AppealMajorStatus setDateSet(String value) {
        appealMajorStatuses.put(XMLTags.date_Set, value);
        return this;
    }

    public AppealMajorStatus setDateClosed(String value) {
        appealMajorStatuses.put(XMLTags.date_Closed, value);
        return this;
    }

    public AppealMajorStatus setBFDate(String value) {
        appealMajorStatuses.put(XMLTags.bf_Date, value);
        return this;
    }

    public Map<String, String> build() {
        return appealMajorStatuses;
    }
}



