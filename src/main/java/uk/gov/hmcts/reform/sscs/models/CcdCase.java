package uk.gov.hmcts.reform.sscs.models;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonRootName("data")
public class CcdCase {
    
    private String caseReference;
    private Appeal appeal;

}
