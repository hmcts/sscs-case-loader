package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Service
class UpdateCcdHearingOptions {
    boolean updateHearingOptions(SscsCaseData gapsCaseData,
                                 SscsCaseData existingCcdCaseData) {
        if (nonNull(gapsCaseData.getAppeal().getHearingOptions())
            && nonNull(gapsCaseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            if (isNull(existingCcdCaseData.getAppeal().getHearingOptions())) {
                existingCcdCaseData.getAppeal().setHearingOptions(HearingOptions.builder().build());
            }
            if (!gapsCaseData.getAppeal().getHearingOptions().getWantsToAttend().equals(
                existingCcdCaseData.getAppeal().getHearingOptions().getWantsToAttend())) {
                existingCcdCaseData.getAppeal().getHearingOptions().setWantsToAttend(
                    gapsCaseData.getAppeal().getHearingOptions().getWantsToAttend());
                return true;
            }
        }
        return false;
    }
}
