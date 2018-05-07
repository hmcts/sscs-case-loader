package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;

@Service
public class PostponedEventInferredFromDelta implements PostponedEventService<Hearing> {
    @Override
    public boolean matchToHearingId(List<PostponementRequests> postponementRequests, List<Hearing> hearingList) {
        return !postponementRequests.stream()
            .filter(postponementRequest -> "Y".equals(postponementRequest.getPostponementGranted()))
            .filter(postponementRequest -> matchToHearingIdInDelta(postponementRequest, hearingList))
            .collect(Collectors.toList())
            .isEmpty();

    }

    private boolean matchToHearingIdInDelta(
        PostponementRequests postponementRequest,
        List<Hearing> hearingList) {
        return !hearingList.stream()
            .filter(hearing -> hearing.getHearingId().equals(postponementRequest.getAppealHearingId()))
            .collect(Collectors.toList())
            .isEmpty();
    }
}
