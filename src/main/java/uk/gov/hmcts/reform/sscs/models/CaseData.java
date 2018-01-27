package uk.gov.hmcts.reform.sscs.models;

public class CaseData {
    public CaseData(Appeal appeal) {
        this.appeal = appeal;
    }

    public Appeal getAppeal() {
        return appeal;
    }

    private Appeal appeal;
}
