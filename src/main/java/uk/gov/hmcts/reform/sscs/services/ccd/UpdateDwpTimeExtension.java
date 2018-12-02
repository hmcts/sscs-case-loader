package uk.gov.hmcts.reform.sscs.services.ccd;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Service
class UpdateDwpTimeExtension {
    boolean updateDwpTimeExtension(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        if (null != gapsCaseData.getDwpTimeExtension() && !gapsCaseData.getDwpTimeExtension().isEmpty()) {
            existingCcdCaseData.setDwpTimeExtension(gapsCaseData.getDwpTimeExtension());
            return true;
        }
        return false;
    }
}
