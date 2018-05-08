package uk.gov.hmcts.reform.sscs.services.mapper;

import java.time.ZonedDateTime;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;

class CaseDataBuilderBase {

    static final String HEARING_POSTPONED_DATE = "2018-05-24T00:00:00+01:00";
    static final String MINOR_STATUS_ID_27_DATE = "2018-05-24T00:00:00+01:00";
    static final String APPEAL_RECEIVED_DATE = "2017-05-24T00:00:00+01:00";

    MajorStatus buildMajorStatusGivenStatusAndDate(String status, String testDate) {
        return new MajorStatus("", status, "", ZonedDateTime.parse(testDate));
    }

    MinorStatus buildMinorStatusGivenIdAndDate(String id, String date) {
        return new MinorStatus("", id, ZonedDateTime.parse(date));
    }

}

