package uk.gov.hmcts.reform.sscs.services.ccd;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Service
class UpdateCcdHearingType {
    boolean updateHearingType(SscsCaseData gaps2CaseData,
                              SscsCaseData existingCcdCaseData) {
        String gaps2HearingType = gaps2CaseData.getAppeal().getHearingType();
        if (StringUtils.isNotBlank(gaps2HearingType)) {
            String ccdHearingType = existingCcdCaseData.getAppeal().getHearingType();
            if (StringUtils.isNotBlank(ccdHearingType)) {
                if (!gaps2HearingType.equals(ccdHearingType)) {
                    existingCcdCaseData.getAppeal().setHearingType(gaps2HearingType);
                    return true;
                }
            } else {
                existingCcdCaseData.getAppeal().setHearingType(gaps2HearingType);
                return true;
            }
        }

        return false;
    }
}
