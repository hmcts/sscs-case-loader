package uk.gov.hmcts.reform.sscs.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CaseData {
    private Appeal appeal;
}
