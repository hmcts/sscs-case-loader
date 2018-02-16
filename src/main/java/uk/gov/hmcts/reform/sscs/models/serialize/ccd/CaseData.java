package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CaseData {
    private String caseReference;
    private Appeal appeal;
    private List<Hearing> hearings;
    private Evidence evidence;
    private List<DwpTimeExtension> dwpTimeExtension;
}
