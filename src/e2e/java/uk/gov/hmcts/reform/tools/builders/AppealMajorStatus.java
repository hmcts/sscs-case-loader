package uk.gov.hmcts.reform.tools.builders;

import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.reform.tools.enums.MajorStatusType;
import uk.gov.hmcts.reform.tools.utils.XmlTags;

public class AppealMajorStatus {

    private Map<String, String> appealMajorStatuses = new HashMap<>();

    public AppealMajorStatus setStatusCode(MajorStatusType majorStatusType) {
        appealMajorStatuses.put(XmlTags.status_Id, majorStatusType.getCode());
        return this;
    }


    public AppealMajorStatus setDateSet(String value) {
        appealMajorStatuses.put(XmlTags.date_Set, value);
        return this;
    }

    public AppealMajorStatus setDateClosed(String value) {
        appealMajorStatuses.put(XmlTags.date_Closed, value);
        return this;
    }

    public AppealMajorStatus setBfDate(String value) {
        appealMajorStatuses.put(XmlTags.bf_Date, value);
        return this;
    }

    public Map<String, String> build() {
        return appealMajorStatuses;
    }
}



