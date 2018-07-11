package uk.gov.hmcts.reform.tools.builders;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.reform.tools.enums.AdminTeam;
import uk.gov.hmcts.reform.tools.enums.BenefitType;
import uk.gov.hmcts.reform.tools.enums.MajorStatusType;
import uk.gov.hmcts.reform.tools.enums.Office;
import uk.gov.hmcts.reform.tools.utils.TestContainer;
import uk.gov.hmcts.reform.tools.utils.XmlTags;

public class AppealHeader {

    private final Date lastUpdatedDate = new Date();
    private final Map<String, String> appealHeader = new HashMap<>();

    public AppealHeader setHeaderDetaults() {
        return setHeaderDefaults(new Date());
    }

    public AppealHeader setHeaderDefaults(Date lastUpdated) {
        setAppealCaseId("3402297");
        setAppealCaseRefNum("SC068/17/4347");
        setAppealCaseConfidential("N");
        setAppealCaseCaseCodeId(BenefitType.PIP);
        setTribunalTypeId("2");
        setAppealCaseNino("JA 00 00 00 A");
        String date = TestContainer.asGapsDate(lastUpdated);
        setAppealCaseBfDate(date);
        setOriginatingOfficeId(Office.NORTH_WEST_LEICSTERSHIRE_DC);
        setAdminTeamId(AdminTeam.LEICESTER_B);
        return this;
    }

    public AppealHeader setExtractTimeUtc(Date value) {
        appealHeader.put(XmlTags.extract_Time_UTC, TestContainer.asGapsDate(value));
        return this;
    }

    public AppealHeader setAppealCaseId(String value) {
        appealHeader.put(XmlTags.appeal_Case_Id, value);
        return this;
    }

    public AppealHeader setAppealCaseRefNum(String value) {
        appealHeader.put(XmlTags.appeal_Case_RefNum, value);
        return this;
    }

    public AppealHeader setAppealCaseConfidential(String value) {
        appealHeader.put(XmlTags.appeal_Case_Confidential, value);
        return this;
    }

    public AppealHeader setAppealCaseCaseCodeId(BenefitType benefitType) {
        appealHeader.put(XmlTags.appeal_Case_Case_Code_Id, benefitType.getCode());
        return this;
    }

    public AppealHeader setTribunalTypeId(String value) {
        appealHeader.put(XmlTags.tribunal_Type_Id, value);
        return this;
    }

    public AppealHeader setAppealCaseDateAppealReceived(String value) {
        appealHeader.put(XmlTags.appeal_Case_Date_Appeal_Received, value);
        return this;
    }

    public AppealHeader setAppealCaseDateOfDecision(String value) {
        appealHeader.put(XmlTags.appeal_Case_Date_of_Decision, value);
        return this;
    }

    public AppealHeader setAppealCaseDateAppealMade(String value) {
        appealHeader.put(XmlTags.appeal_Case_Date_Appeal_Made, value);
        return this;
    }

    public AppealHeader setAppealCaseNino(String value) {
        appealHeader.put(XmlTags.appeal_Case_NINO, value);
        return this;
    }

    public AppealHeader setAppealCaseMajorId(MajorStatusType majorStatusType) {
        appealHeader.put(XmlTags.appeal_Case_Major_Id, majorStatusType.getCode());
        return this;
    }

    public AppealHeader setAppealCaseBfDate(String value) {
        appealHeader.put(XmlTags.appeal_Case_BF_Date, value);
        return this;
    }

    public AppealHeader setOriginatingOfficeId(Office office) {
        appealHeader.put(XmlTags.originating_Office_Id, office.getCode());
        return this;
    }

    public AppealHeader setAdminTeamId(AdminTeam adminTeam) {
        appealHeader.put(XmlTags.admin_Team_Id, adminTeam.getCode());
        return this;
    }

    public Map<String, String> build() {

        return appealHeader;
    }
}
