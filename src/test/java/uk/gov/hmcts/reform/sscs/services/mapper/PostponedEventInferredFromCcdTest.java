package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.HearingDetails;

@RunWith(JUnitParamsRunner.class)
public class PostponedEventInferredFromCcdTest {

    @Test
    @Parameters({"1, 1, true", "1, 2, false"})
    public void givenPostponedRequestAndHearingThenReturnTrueIfThereIsAMatch(String appealHearingId,
                                                                             String hearingId, boolean expected) {
        PostponedEventInferredFromCcd postponedEventInferredFromCcd = new PostponedEventInferredFromCcd();

        List<PostponementRequests> postponementRequests = Collections.singletonList(
            new PostponementRequests("Y", appealHearingId, null,
                null)
        );

        List<Hearing> hearingList = Collections.singletonList(Hearing.builder()
            .value(HearingDetails.builder()
                .hearingId(hearingId)
                .build())
            .build());

        boolean actual = postponedEventInferredFromCcd.matchToHearingId(postponementRequests, hearingList);

        assertEquals(expected, actual);
    }
}
