package uk.gov.hmcts.reform.sscs.services.mapper;

import java.time.ZonedDateTime;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;

class CaseDataBuilderBaseTest {

    static final String TEST_DATE2 = "2018-05-24T00:00:00+01:00";
    static final String TEST_DATE = "2017-05-24T00:00:00+01:00";

    MajorStatus buildMajorStatusGivenStatusAndDate(String status, String testDate) {
        return new MajorStatus("", status, "", ZonedDateTime.parse(testDate));
    }

    MinorStatus buildMinorStatusGivenIdAndDate(String id, ZonedDateTime date) {
        return new MinorStatus("", id, date);
    }

}

