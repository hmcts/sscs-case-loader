package uk.gov.hmcts.reform.sscs.services.mapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;

class CaseDataBuilderBaseTest {

    static final String TEST_DATE2 = "2018-05-24T00:00:00+01:00";
    static final String TEST_DATE = "2017-05-24T00:00:00+01:00";

    List<MajorStatus> buildMajorStatusGivenStatuses(GapsEvent... gapsEvents) {
        ArrayList<MajorStatus> majorStatusList = new ArrayList<>(gapsEvents.length);
        for (GapsEvent gapsEvent : gapsEvents) {
            if (gapsEvent.equals(GapsEvent.APPEAL_RECEIVED)) {
                majorStatusList.add(buildMajorStatusGivenDate(gapsEvent.getStatus(), TEST_DATE));
            }
            if (gapsEvent.equals(GapsEvent.HEARING_POSTPONED)) {
                majorStatusList.add(buildMajorStatusGivenDate(gapsEvent.getStatus(), TEST_DATE2));
            }
        }
        return majorStatusList;
    }

    private MajorStatus buildMajorStatusGivenDate(String status, String testDate) {
        return new MajorStatus("", status, "", ZonedDateTime.parse(testDate));
    }

    List<MinorStatus> getMinorStatusId26(String testDate) {
        return Collections.singletonList(
            new MinorStatus("", "26", ZonedDateTime.parse(testDate)));
    }

}

