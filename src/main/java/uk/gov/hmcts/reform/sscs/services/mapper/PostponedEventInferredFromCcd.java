package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;

@Service
public class PostponedEventInferredFromCcd implements PostponedEventService<Hearing> {
    @Override
    public boolean matchToHearingId(List<PostponementRequests> postponementRequests, List<Hearing> hearingList) {
        if (hearingList != null && !hearingList.isEmpty()
            && postponementRequests != null && !postponementRequests.isEmpty()) {
            return !postponementRequests.stream()
                .filter(postponementRequest -> "Y".equals(postponementRequest.getPostponementGranted()))
                .filter(postponementRequest -> matchToHearingIdInCcdCase(postponementRequest, hearingList))
                .collect(Collectors.toList())
                .isEmpty();
        }
        return false;
    }

    private boolean matchToHearingIdInCcdCase(PostponementRequests postponementRequest, List<Hearing> hearingList) {
        if (postponementRequest != null && postponementRequest.getAppealHearingId() != null) {
            return !hearingList.stream()
                .filter(hearing -> isHearingIdEqualToPostponedHearingId(postponementRequest, hearing))
                .collect(Collectors.toList())
                .isEmpty();
        }
        return false;
    }

    private boolean isHearingIdEqualToPostponedHearingId(PostponementRequests postponementRequest, Hearing hearing) {
        if (hearing != null && hearing.getValue() != null && hearing.getValue().getHearingId() != null) {
            return hearing.getValue().getHearingId().equals(postponementRequest.getAppealHearingId());
        }
        return false;
    }
}
