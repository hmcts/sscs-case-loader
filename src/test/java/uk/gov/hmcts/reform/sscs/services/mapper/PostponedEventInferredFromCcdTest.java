package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;

@RunWith(JUnitParamsRunner.class)
public class PostponedEventInferredFromCcdTest {

    @Test
    @Parameters({"1, 1, true", "1, 2, false", "null, 1, false", "1, null, false"})
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

    @Test
    @Parameters(method = "getHearingListAndAppealHearingIdParameters")
    public void givenHearingListAndAppealHearingIdAreEmptyOrNullThenReturnFalse(List<Hearing> hearingList,
                                                                                String appealHearingId) {

        PostponedEventInferredFromCcd postponedEventInferredFromCcd = new PostponedEventInferredFromCcd();

        List<PostponementRequests> postponementRequests = Collections.singletonList(
            new PostponementRequests("Y", appealHearingId, null,
                null)
        );

        boolean actual = postponedEventInferredFromCcd.matchToHearingId(postponementRequests, hearingList);

        assertFalse(actual);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object getHearingListAndAppealHearingIdParameters() {
        return new Object[]{
            new Object[]{null, null},
            new Object[]{null, "1"},
            new Object[]{Collections.emptyList(), Collections.emptyList()},
            new Object[]{Collections.singletonList(Hearing.builder()
                .value(HearingDetails.builder()
                    .hearingId(null)
                    .build())
                .build()), null},
            new Object[]{Collections.singletonList(Hearing.builder()
                .value(HearingDetails.builder()
                    .build())
                .build()), Collections.emptyList()}
        };
    }
}
