package uk.gov.hmcts.reform.sscs.services.mapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;

@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
class CaseDataBuilderBaseTest {

    private static final String TEST_DATE2 = "2018-05-24T00:00:00+01:00";
    private static final String TEST_DATE = "2017-05-24T00:00:00+01:00";

    List<MajorStatus> buildMajorStatusGivenStatuses(GapsEvent... gapsEvents) {
        ArrayList<MajorStatus> majorStatusList = new ArrayList<>(gapsEvents.length);
        for (GapsEvent gapsEvent : gapsEvents) {
            if (gapsEvent.equals(GapsEvent.APPEAL_RECEIVED)) {
                majorStatusList.add(new MajorStatus("", gapsEvent.getStatus(), "",
                    ZonedDateTime.parse(TEST_DATE))); //NOPMD
            }
            if (gapsEvent.equals(GapsEvent.HEARING_POSTPONED)) {
                majorStatusList.add(new MajorStatus("", gapsEvent.getStatus(), "",
                    ZonedDateTime.parse(TEST_DATE2))); //NOPMD
            }
        }
        return majorStatusList;
    }

    List<MinorStatus> getMinorStatusId26() {
        return Collections.singletonList(
            new MinorStatus("", "26", ZonedDateTime.parse(TEST_DATE2)));
    }

}
