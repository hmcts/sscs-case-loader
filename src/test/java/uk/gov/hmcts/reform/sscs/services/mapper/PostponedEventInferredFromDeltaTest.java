package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;

public class PostponedEventInferredFromDeltaTest {
    @Test
    public void givenPostponedRequestMatchesToHearingThenReturnTrue() {
        PostponedEventInferredFromDelta postponedEventInferredFromDelta = new PostponedEventInferredFromDelta();

        List<PostponementRequests> postponementRequests = Collections.singletonList(
            new PostponementRequests("Y", "1", null,
                null)
        );
        List<Hearing> hearingList = Collections.singletonList(Hearing.builder().hearingId("1").build());

        boolean actual = postponedEventInferredFromDelta.matchToHearingId(postponementRequests, hearingList);
        assertTrue(actual);
    }
}
