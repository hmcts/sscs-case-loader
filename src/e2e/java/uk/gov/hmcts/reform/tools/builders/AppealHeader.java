package uk.gov.hmcts.reform.tools.builders;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.reform.tools.enums.AdminTeam;
import uk.gov.hmcts.reform.tools.enums.BenefitType;
import uk.gov.hmcts.reform.tools.enums.MajorStatusType;
import uk.gov.hmcts.reform.tools.enums.Office;
import uk.gov.hmcts.reform.tools.utils.TestContainer;
import uk.gov.hmcts.reform.tools.utils.XMLTags;

public class AppealHeader {

    public AppealHeader setHeaderDetaults() {
        return setHeaderDefaults(new Date());
    }

    private Map<String, String> appealHeader = new HashMap<>();
    private Date lastUpdatedDate = new Date();

    public AppealHeader setHeaderDefaults(Date lastUpdated) {

        String date = TestContainer.asGapsDate(lastUpdated);
        setAppealCaseId("3402297");
        setAppealCaseRefNum("SC068/17/4347");
        setAppealCaseConfidential("N");
        setAppealCaseCaseCodeId(BenefitType.PIP);
        setTribunalTypeId("2");
        setAppealCaseNINO("JA 00 00 00 A");
        setAppealCaseBFDate(date);
        setOriginatingOfficeId(Office.NORTH_WEST_LEICSTERSHIRE_DC);
        setAdminTeamId(AdminTeam.LEICESTER_B);
        return this;
    }

    public AppealHeader setExtractTimeUTC(Date value) {
        appealHeader.put(XMLTags.extract_Time_UTC, TestContainer.asGapsDate(value));
        return this;
    }

    public AppealHeader setAppealCaseId(String value) {
        appealHeader.put(XMLTags.appeal_Case_Id, value);
        return this;
    }

    public String getAppealCaseId() {
        return appealHeader.get(XMLTags.appeal_Case_Id);
    }

    public String getAppealCaseRefNum() {
        return appealHeader.get(XMLTags.appeal_Case_RefNum);
    }

    public AppealHeader setAppealCaseRefNum(String value) {
        appealHeader.put(XMLTags.appeal_Case_RefNum, value);
        return this;
    }

    public AppealHeader setAppealCaseConfidential(String value) {
        appealHeader.put(XMLTags.appeal_Case_Confidential, value);
        return this;
    }

    public AppealHeader setAppealCaseCaseCodeId(BenefitType benefitType) {
        appealHeader.put(XMLTags.appeal_Case_Case_Code_Id, benefitType.getCode());
        return this;
    }

    public AppealHeader setTribunalTypeId(String value) {
        appealHeader.put(XMLTags.tribunal_Type_Id, value);
        return this;
    }

    public AppealHeader setAppealCaseDateAppealReceived(Integer value) {
        Date date = TestContainer.backDate(lastUpdatedDate, value);
        String dateString = TestContainer.asGapsDate(date);
        appealHeader.put(XMLTags.appeal_Case_Date_Appeal_Received, dateString);
        return this;
    }

    public AppealHeader setAppealCaseDateAppealReceived(String value) {
        appealHeader.put(XMLTags.appeal_Case_Date_Appeal_Received, value);
        return this;
    }

    public AppealHeader setAppealCaseDateOfDecision(Integer value) {
        Date date = TestContainer.backDate(lastUpdatedDate, value);
        String dateString = TestContainer.asGapsDate(date);
        appealHeader.put(XMLTags.appeal_Case_Date_of_Decision, dateString);
        return this;
    }

    public AppealHeader setAppealCaseDateOfDecision(String value) {
        appealHeader.put(XMLTags.appeal_Case_Date_of_Decision, value);
        return this;
    }

    public AppealHeader setAppealCaseDateAppealMade(Integer value) {
        Date date = TestContainer.backDate(lastUpdatedDate, value);
        String dateString = TestContainer.asGapsDate(date);
        appealHeader.put(XMLTags.appeal_Case_Date_Appeal_Made, dateString);
        return this;
    }

    public AppealHeader setAppealCaseDateAppealMade(String value) {
        appealHeader.put(XMLTags.appeal_Case_Date_Appeal_Made, value);
        return this;
    }

    public AppealHeader setAppealCaseNINO(String value) {
        appealHeader.put(XMLTags.appeal_Case_NINO, value);
        return this;
    }

    public AppealHeader setAppealCaseMajorId(MajorStatusType majorStatusType) {
        appealHeader.put(XMLTags.appeal_Case_Major_Id, majorStatusType.getCode());
        return this;
    }

    public AppealHeader setAppealCaseBFDate(String value) {
        appealHeader.put(XMLTags.appeal_Case_BF_Date, value);
        return this;
    }

    public AppealHeader setOriginatingOfficeId(Office office) {
        appealHeader.put(XMLTags.originating_Office_Id, office.getCode());
        return this;
    }

    public AppealHeader setAdminTeamId(AdminTeam adminTeam) {
        appealHeader.put(XMLTags.admin_Team_Id, adminTeam.getCode());
        return this;
    }

    public Map<String, String> build() {

        return appealHeader;
    }
}
