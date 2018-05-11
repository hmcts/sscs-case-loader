package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.List;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;

public interface PostponedEventService<T> {
    boolean matchToHearingId(List<PostponementRequests> postponementRequests, List<T> hearingList);
}
