package uk.gov.hmcts.reform.sscs.services.ccd;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Service
class UpdateCcdHearingOptions {
    boolean updateHearingOptions(SscsCaseData gapsCaseData,
                                 SscsCaseData existingCcdCaseData) {
        String gaps2WantsToAttend = null;
        String existingCcdWantsToAttend = null;

        if (null != gapsCaseData.getAppeal().getHearingOptions()
            && StringUtils.isNotBlank(gapsCaseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            gaps2WantsToAttend = gapsCaseData.getAppeal().getHearingOptions().getWantsToAttend();
        }

        if (null != existingCcdCaseData.getAppeal().getHearingOptions()
            && StringUtils.isNotBlank(existingCcdCaseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            existingCcdWantsToAttend = existingCcdCaseData.getAppeal().getHearingOptions().getWantsToAttend();
        }

        if (StringUtils.isNotBlank(gaps2WantsToAttend)) {
            if (StringUtils.isNotBlank(existingCcdWantsToAttend)) {
                if (!gaps2WantsToAttend.equals(existingCcdWantsToAttend)) {
                    existingCcdCaseData.getAppeal().getHearingOptions().setWantsToAttend(gaps2WantsToAttend);
                    return true;
                }
            } else {
                existingCcdCaseData.getAppeal().setHearingOptions(HearingOptions.builder()
                    .wantsToAttend(gaps2WantsToAttend)
                    .build());
                return true;
            }
        }
        return false;
    }
}
