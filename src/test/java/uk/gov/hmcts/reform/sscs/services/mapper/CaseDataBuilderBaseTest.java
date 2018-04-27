package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;

public class CaseDataBuilderBaseTest {

    static final String TEST_DATE2 = "2018-05-24T00:00:00+01:00";
    static final String TEST_DATE = "2017-05-24T00:00:00+01:00";

    private AppealCase appeal;

    @Before
    public void setUp() {
        appeal = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(getStatus())
            .hearing(getHearing())
            .minorStatus(Collections.singletonList(
                new MinorStatus("", "26", ZonedDateTime.parse(TEST_DATE2))))
            .build();
    }

    public List<MajorStatus> getStatus() {
        MajorStatus status = new MajorStatus("", "3", "", ZonedDateTime.parse(TEST_DATE));
        return newArrayList(status);
    }

    public List<Hearing> getHearing() {
        Hearing hearing = new Hearing("outcome",
            "venue",
            "outcomeDate",
            "notificationDate",
            "2017-05-24T00:00:00+01:00",
            "2017-05-24T10:30:00+01:00",
            "id");
        return newArrayList(hearing);
    }

    public AppealCase getAppeal() {
        return appeal;
    }
}
