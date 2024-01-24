package uk.gov.hmcts.reform.sscs.services.ccd;

import static uk.gov.hmcts.reform.sscs.exceptions.FeignExceptionLogger.debugCaseLoaderException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Slf4j
@Service
class UpdateCcdHearingType {
    boolean updateHearingType(SscsCaseData gapsCaseData,
                              SscsCaseData existingCcdCaseData) {
        String gaps2HearingType = gapsCaseData.getAppeal().getHearingType();
        if (StringUtils.isNotBlank(gaps2HearingType)) {
            String ccdHearingType = existingCcdCaseData.getAppeal().getHearingType();
            if (StringUtils.isNotBlank(ccdHearingType)) {
                if (!HearingType.COR.getValue().equalsIgnoreCase(ccdHearingType)
                    && !HearingType.DOMICILIARY.getValue().equalsIgnoreCase(gaps2HearingType)) {
                    if (!gaps2HearingType.equals(ccdHearingType)) {
                        try {
                            existingCcdCaseData.getAppeal().setHearingType(gaps2HearingType);
                            log.info("Updated hearing type from {} to {} for case {}", gaps2HearingType,
                                ccdHearingType, existingCcdCaseData.getCcdCaseId());
                        } catch (FeignException e) {
                            debugCaseLoaderException(log, e, "Could not update hearing type");
                        }
                        return true;
                    }
                }
            } else {
                existingCcdCaseData.getAppeal().setHearingType(gaps2HearingType);
                return true;
            }
        }
        return false;
    }
}
