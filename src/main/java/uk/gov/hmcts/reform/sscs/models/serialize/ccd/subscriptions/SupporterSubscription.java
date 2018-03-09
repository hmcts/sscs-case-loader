package uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SupporterSubscription {
    String tya;
    String email;
    String mobile;
    String subscribeEmail;
    String subscribeSms;
    String reason;
}
