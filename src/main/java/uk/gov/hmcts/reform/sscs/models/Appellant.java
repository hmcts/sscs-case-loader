package uk.gov.hmcts.reform.sscs.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Appellant {
    
    private Name name;
    private Identity identity; 

}
