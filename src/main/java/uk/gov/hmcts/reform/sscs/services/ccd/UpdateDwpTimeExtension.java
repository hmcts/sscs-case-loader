package uk.gov.hmcts.reform.sscs.services.ccd;

import static uk.gov.hmcts.reform.sscs.exceptions.FeignExceptionLogger.debugCaseLoaderException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Slf4j
@Service
class UpdateDwpTimeExtension {
    boolean updateDwpTimeExtension(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        if (null != gapsCaseData.getDwpTimeExtension() && !gapsCaseData.getDwpTimeExtension().isEmpty()) {
            try {
                existingCcdCaseData.setDwpTimeExtension(gapsCaseData.getDwpTimeExtension());
                return true;
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Time Extension");
            }
        }
        return false;
    }
}
