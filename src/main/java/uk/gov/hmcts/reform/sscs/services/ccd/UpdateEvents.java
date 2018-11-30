package uk.gov.hmcts.reform.sscs.services.ccd;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Service
class UpdateEvents {
    boolean update(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        if (thereIsAnEventChange(gapsCaseData, existingCcdCaseData)) {
            existingCcdCaseData.setEvents(gapsCaseData.getEvents());
            return true;
        }
        return false;
    }

    private boolean thereIsAnEventChange(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        if (caseData == null || caseData.getEvents() == null) {
            return false;
        }
        if (existingCcdCaseData == null) {
            return true;
        }

        return existingCcdCaseData.getEvents() == null
            || caseData.getEvents().size() != existingCcdCaseData.getEvents().size();
    }

}
