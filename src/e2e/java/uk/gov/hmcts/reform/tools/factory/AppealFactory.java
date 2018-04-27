package uk.gov.hmcts.reform.tools.factory;

import java.io.IOException;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import uk.gov.hmcts.reform.tools.builders.Appeal;
import uk.gov.hmcts.reform.tools.enums.*;
import uk.gov.hmcts.reform.tools.utils.AppealRandomValues;
import uk.gov.hmcts.reform.tools.utils.XmlWriter;

@SuppressWarnings({"PMD", "checkstyle:hideutilityclassconstructor"})

public class AppealFactory {

    public XmlWriter selectAppeal(XmlWriter xmlWriter, AppealTemplate appealTemplate, int requiredNumber)
            throws IOException, TransformerException, ParserConfigurationException {

        for (int i = 0; i < requiredNumber; i++) {
            selectAppeal(xmlWriter, appealTemplate);
        }
        return xmlWriter;
    }

    public XmlWriter selectAppeal(XmlWriter xmlWriter, AppealTemplate appealTemplate) throws
            IOException, TransformerException, ParserConfigurationException {

        switch (appealTemplate) {
            case NEW_DIRECT_LODGEMENT:
                return newDirectLodgement(xmlWriter);
            case APPEAL_AWAITING_RESPONSE:
                return newAppealAwaitingResponse(xmlWriter);
            case READY_TO_LIST:
                return newReadyToList(xmlWriter);
            case LISTED_FOR_HEARING:
                return newListedForHearing(xmlWriter);
            case HEARD_FOR_DESTRUCTION:
                return newHeardForDestruction(xmlWriter);
            default:
                break;
        }
        return xmlWriter;
    }

    private XmlWriter newDirectLodgement(XmlWriter xmlWriter) throws
            ParserConfigurationException, TransformerException, IOException {

        AppealRandomValues a = new AppealRandomValues("SC001/0");

        Appeal appeal = new Appeal(xmlWriter);
        appeal.setStartedDaysAGo(6);
        appeal.setUpdatedDaysAGo(0);

        appeal.header

                .setExtractTimeUtc(new Date())
                .setAppealCaseId(a.caseId)
                .setAppealCaseRefNum(a.caseRefNumber)
                .setAppealCaseConfidential("N")
                .setAppealCaseCaseCodeId(BenefitType.PIP)
                .setTribunalTypeId("2")
                .setAppealCaseDateAppealReceived(appeal.onDay(1))
                .setAppealCaseDateOfDecision(appeal.getLastUpdatedDate())
                .setAppealCaseDateAppealMade(appeal.onDay(0))
                .setAppealCaseNino("JA 00 00 00 A")
                .setAppealCaseMajorId(MajorStatusType.NEW_DIRECT_LODGEMENT)
                .setAppealCaseBfDate(appeal.getLastUpdatedDate())
                .setOriginatingOfficeId(Office.NORTH_WEST_LEICSTERSHIRE_DC)
                .setAdminTeamId(AdminTeam.LEICESTER_B);

        appeal.storeHeader();

        appeal.parties
                .setPttp_Id(PartyPptp.APPELLANT)
                .setTitle("Mr")
                .setInitials("A")
                .setSurname(a.sureName)
                .setPostcode("HA5 1ND")
                .setRoleId(PartyType.APPELLANT)
                .setAttending("Y")
                .setDisabilityNeeds("N");
        appeal.storeParties();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.NEW_DIRECT_LODGEMENT)
                .setDateSet(appeal.onDay(0))
                .setBfDate(appeal.onDay(0));
        appeal.storeMajorStatus();

        xmlWriter = appeal.write();
        xmlWriter.pushAppealToBeWritten(appeal);

        return xmlWriter;

    }

    private XmlWriter newAppealAwaitingResponse(XmlWriter xmlWriter) throws
            ParserConfigurationException, TransformerException, IOException {
        AppealRandomValues a = new AppealRandomValues("SC002/0");

        Appeal appeal = new Appeal(xmlWriter);
        appeal.setStartedDaysAGo(6);
        appeal.setUpdatedDaysAGo(0);

        appeal.header

                .setExtractTimeUtc(new Date())
                .setAppealCaseId(a.caseId)
                .setAppealCaseRefNum(a.caseRefNumber)
                .setAppealCaseConfidential("N")
                .setAppealCaseCaseCodeId(BenefitType.PIP)
                .setTribunalTypeId("2")
                .setAppealCaseDateAppealReceived(appeal.onDay(1))
                .setAppealCaseDateOfDecision(appeal.getLastUpdatedDate())
                .setAppealCaseDateAppealMade(appeal.onDay(0))
                .setAppealCaseNino("JA 00 00 00 A")
                .setAppealCaseMajorId(MajorStatusType.AWAITING_RESPONSE)
                .setAppealCaseBfDate(appeal.getLastUpdatedDate())
                .setOriginatingOfficeId(Office.NORTH_WEST_LEICSTERSHIRE_DC)
                .setAdminTeamId(AdminTeam.LEICESTER_B);

        appeal.storeHeader();

        appeal.parties
                .setPttp_Id(PartyPptp.APPELLANT)
                .setTitle("Mr")
                .setInitials("B")
                .setSurname(a.sureName)
                .setPostcode("HA5 1NC")
                .setRoleId(PartyType.APPELLANT)
                .setAttending("Y")
                .setDisabilityNeeds("N");
        appeal.storeParties();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.NEW_DIRECT_LODGEMENT)
                .setDateSet(appeal.onDay(1))
                .setBfDate(appeal.onDay(1))
                .setDateClosed(appeal.onDay(0));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.AWAITING_RESPONSE)
                .setDateSet(appeal.onDay(0))
                .setBfDate(appeal.onDay(0));
        appeal.storeMajorStatus();

        xmlWriter = appeal.write();
        xmlWriter.pushAppealToBeWritten(appeal);

        return xmlWriter;

    }

    private XmlWriter newReadyToList(XmlWriter xmlWriter) throws
            ParserConfigurationException, TransformerException, IOException {

        AppealRandomValues a = new AppealRandomValues("SC003/0");

        Appeal appeal = new Appeal(xmlWriter);
        appeal.setStartedDaysAGo(6);
        appeal.setUpdatedDaysAGo(0);

        appeal.header

                .setExtractTimeUtc(new Date())
                .setAppealCaseId(a.caseId)
                .setAppealCaseRefNum(a.caseRefNumber)
                .setAppealCaseConfidential("N")
                .setAppealCaseCaseCodeId(BenefitType.PIP)
                .setTribunalTypeId("2")
                .setAppealCaseDateAppealReceived(appeal.onDay(1))
                .setAppealCaseDateOfDecision(appeal.getLastUpdatedDate())
                .setAppealCaseDateAppealMade(appeal.onDay(0))
                .setAppealCaseNino("JA 00 00 00 A")
                .setAppealCaseMajorId(MajorStatusType.READY_TO_LIST)
                .setAppealCaseBfDate(appeal.getLastUpdatedDate())
                .setOriginatingOfficeId(Office.NORTH_WEST_LEICSTERSHIRE_DC)
                .setAdminTeamId(AdminTeam.LEICESTER_B);

        appeal.storeHeader();

        appeal.parties
                .setPttp_Id(PartyPptp.APPELLANT)
                .setTitle("Mr")
                .setInitials("C")
                .setSurname(a.sureName)
                .setPostcode("HA5 1ND")
                .setRoleId(PartyType.APPELLANT)
                .setAttending("Y")
                .setDisabilityNeeds("N");
        appeal.storeParties();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.NEW_DIRECT_LODGEMENT)
                .setDateSet(appeal.onDay(3))
                .setBfDate(appeal.onDay(3))
                .setDateClosed(appeal.onDay(2));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.AWAITING_RESPONSE)
                .setDateSet(appeal.onDay(1))
                .setBfDate(appeal.onDay(1))
                .setDateClosed(appeal.onDay(0));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.READY_TO_LIST)
                .setDateSet(appeal.onDay(0))
                .setBfDate(appeal.onDay(0));
        appeal.storeMajorStatus();

        xmlWriter = appeal.write();
        xmlWriter.pushAppealToBeWritten(appeal);

        return xmlWriter;
    }

    public XmlWriter newListedForHearing(XmlWriter xmlWriter) throws
            ParserConfigurationException, TransformerException, IOException {
        AppealRandomValues a = new AppealRandomValues("SC004/0");

        Appeal appeal = new Appeal(xmlWriter);
        appeal.setStartedDaysAGo(6);
        appeal.setUpdatedDaysAGo(0);

        appeal.header
                .setExtractTimeUtc(new Date())
                .setAppealCaseId(a.caseId)
                .setAppealCaseRefNum(a.caseRefNumber)
                .setAppealCaseConfidential("N")
                .setAppealCaseCaseCodeId(BenefitType.PIP)
                .setTribunalTypeId("2")
                .setAppealCaseDateAppealReceived(appeal.onDay(1))
                .setAppealCaseDateOfDecision(appeal.getLastUpdatedDate())
                .setAppealCaseDateAppealMade(appeal.onDay(0))
                .setAppealCaseNino("JA 00 00 00 A")
                .setAppealCaseMajorId(MajorStatusType.LISTED_FOR_HEARING)
                .setAppealCaseBfDate(appeal.getLastUpdatedDate())
                .setOriginatingOfficeId(Office.NORTH_WEST_LEICSTERSHIRE_DC)
                .setAdminTeamId(AdminTeam.LEICESTER_B);

        appeal.storeHeader();

        appeal.parties
                .setPttp_Id(PartyPptp.APPELLANT)
                .setTitle("Mr")
                .setInitials("D")
                .setSurname(a.sureName)
                .setPostcode("HA4 1ER")
                .setRoleId(PartyType.APPELLANT)
                .setAttending("Y")
                .setDisabilityNeeds("N");
        appeal.storeParties();

        appeal.hearing
                .setHearingId(a.hearingId)
                .setDateHearingNotification(appeal.getFutureDate(1))
                .setDateOutcomeDescisionNotification(appeal.getFutureDate(1))
                .setSessionDate(appeal.getFutureDate(5))
                .setAppealTime(appeal.getFutureDate(5))
                .setVenueId("1")
                .setOutcome(OutcomeType.UNSET)
                .build();

        appeal.storeHearing();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.NEW_DIRECT_LODGEMENT)
                .setDateSet(appeal.onDay(1))
                .setBfDate(appeal.onDay(1))
                .setDateClosed(appeal.onDay(2));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.AWAITING_RESPONSE)
                .setDateSet(appeal.onDay(2))
                .setBfDate(appeal.onDay(3))
                .setDateClosed(appeal.onDay(3));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.READY_TO_LIST)
                .setDateSet(appeal.onDay(4))
                .setBfDate(appeal.onDay(4))
                .setDateClosed(appeal.onDay(3));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.LISTED_FOR_HEARING)
                .setDateSet(appeal.onDay(0))
                .setBfDate(appeal.onDay(0))
                .setDateClosed(appeal.onDay(0));
        appeal.storeMajorStatus();

        xmlWriter = appeal.write();
        xmlWriter.pushAppealToBeWritten(appeal);

        return xmlWriter;
    }

    public XmlWriter newHeardForDestruction(XmlWriter xmlWriter) throws
            ParserConfigurationException, TransformerException, IOException {

        AppealRandomValues a = new AppealRandomValues("SC005/0");

        Appeal appeal = new Appeal(xmlWriter);
        appeal.setStartedDaysAGo(6);
        appeal.setUpdatedDaysAGo(0);

        appeal.header

                .setExtractTimeUtc(new Date())
                .setAppealCaseId(a.caseId)
                .setAppealCaseRefNum(a.caseRefNumber)
                .setAppealCaseConfidential("N")
                .setAppealCaseCaseCodeId(BenefitType.PIP)
                .setTribunalTypeId("2")
                .setAppealCaseDateAppealReceived(appeal.onDay(1))
                .setAppealCaseDateOfDecision(appeal.getLastUpdatedDate())
                .setAppealCaseDateAppealMade(appeal.onDay(0))
                .setAppealCaseNino("JA 00 00 00 A")
                .setAppealCaseMajorId(MajorStatusType.DORMANT_APPEAL_HEARD_FOR_DESTRUCTION)
                .setAppealCaseBfDate(appeal.getLastUpdatedDate())
                .setOriginatingOfficeId(Office.NORTH_WEST_LEICSTERSHIRE_DC)
                .setAdminTeamId(AdminTeam.LEICESTER_B);

        appeal.storeHeader();

        appeal.parties
                .setPttp_Id(PartyPptp.APPELLANT)
                .setTitle("Mr")
                .setInitials("E")
                .setSurname(a.sureName)
                .setPostcode("HA6 1TR")
                .setRoleId(PartyType.APPELLANT)
                .setAttending("Y")
                .setDisabilityNeeds("N");
        appeal.storeParties();

        appeal.hearing
                .setHearingId(a.hearingId)
                .setDateHearingNotification(appeal.onDay(6))
                .setDateOutcomeDescisionNotification(appeal.onDay(6))
                .setSessionDate(appeal.onDay(8))
                .setAppealTime(appeal.onDay(8))
                .setVenueId("1")
                .setOutcome(OutcomeType.UNSET)
                .build();

        appeal.storeHearing();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.NEW_DIRECT_LODGEMENT)
                .setDateSet(appeal.onDay(1))
                .setBfDate(appeal.onDay(1))
                .setDateClosed(appeal.onDay(2));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.AWAITING_RESPONSE)
                .setDateSet(appeal.onDay(2))
                .setBfDate(appeal.onDay(2))
                .setDateClosed(appeal.onDay(3));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.READY_TO_LIST)
                .setDateSet(appeal.onDay(4))
                .setBfDate(appeal.onDay(4))
                .setDateClosed(appeal.onDay(5));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.LISTED_FOR_HEARING)
                .setDateSet(appeal.onDay(5))
                .setBfDate(appeal.onDay(5))
                .setDateClosed(appeal.onDay(8));
        appeal.storeMajorStatus();

        appeal.majorStatus
                .setStatusCode(MajorStatusType.DORMANT_APPEAL_HEARD_FOR_DESTRUCTION)
                .setDateSet(appeal.onDay(8))
                .setBfDate(appeal.onDay(8));
        appeal.storeMajorStatus();

        xmlWriter = appeal.write();
        xmlWriter.pushAppealToBeWritten(appeal);

        return xmlWriter;

    }

}
