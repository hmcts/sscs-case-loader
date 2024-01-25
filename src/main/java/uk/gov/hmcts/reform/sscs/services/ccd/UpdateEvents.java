package uk.gov.hmcts.reform.sscs.services.ccd;

import static uk.gov.hmcts.reform.sscs.exceptions.FeignExceptionLogger.debugCaseLoaderException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Slf4j
@Service
class UpdateEvents {
    boolean update(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        if (thereIsAnEventChange(gapsCaseData, existingCcdCaseData)) {
            try {
                existingCcdCaseData.setEvents(gapsCaseData.getEvents());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Events");
            }
            return true;
        }
        return false;
    }

    private boolean thereIsAnEventChange(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        if (caseData == null || caseData.getEvents() == null) {
            return false;
        }
        return existingCcdCaseData.getEvents() == null
            || caseData.getEvents().size() != existingCcdCaseData.getEvents().size();
    }

}
