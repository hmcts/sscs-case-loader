package uk.gov.hmcts.reform.tools.utils;

public class AppealRandomValues {

    public final String caseId;
    public final String caseRefNumber;
    public final String surName;
    public final String hearingId;

    public AppealRandomValues(String testRef) {
        Utils utils = new Utils();
        caseId = utils.getNewCaseId();
        caseRefNumber = utils.getCaseRefNumber(testRef, caseId);
        surName = utils.getSurname(caseId);
        hearingId = utils.getNewHearingId();
    }

}