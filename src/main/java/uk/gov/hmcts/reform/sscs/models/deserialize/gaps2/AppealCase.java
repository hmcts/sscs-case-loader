package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import static com.google.common.collect.Lists.newArrayList;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AppealCase {

    private static List<String> appealRecieved = newArrayList(
        "3", "4", "9",
        "11", "14", "86",
        "93", "187", "204",
        "401", "600", "602",
        "603", "604", "605", "608");

    String appealCaseDateOfDecision;
    String adminTeamId;
    String originatingOfficeId;
    String appealCaseDateFtaResponseReceived;
    List<FurtherEvidence> furtherEvidence;
    String appealCaseRefNum;
    List<MajorStatus> majorStatus;
    String extractTimeUtc;
    String appealCaseBfDate;
    List<Parties> parties;
    List<Hearing> hearing;
    String tribunalTypeId;
    String appealCaseNino;
    String appealCaseId;
    String appealCaseMajorId;
    String appealCaseCaseCodeId;
    String appealCaseDateAppealMade;
    String appealCaseDateAppealReceived;
    List<PostponementRequests> postponementRequests;
    List<MinorStatus> minorStatus;
    String additionalRef;

    public AppealCase(@JsonAlias("Appeal_Case_Date_of_Decision") String appealCaseDateOfDecision,
                      @JsonAlias("Admin_Team_Id") String adminTeamId,
                      @JsonAlias("Originating_Office_Id") String originatingOfficeId,
                      @JsonAlias("Appeal_Case_Date_FTA_Response_Received") String appealCaseDateFtaResponseReceived,
                      @JsonAlias("Further_Evidence") List<FurtherEvidence> furtherEvidence,
                      @JsonAlias("Appeal_Case_RefNum") String appealCaseRefNum,
                      @JsonAlias("Major_Status") List<MajorStatus> majorStatus,
                      @JsonAlias("Extract_Time_UTC") String extractTimeUtc,
                      @JsonAlias("Appeal_Case_BF_Date") String appealCaseBfDate,
                      @JsonAlias("Parties") List<Parties> parties,
                      @JsonAlias("Hearing") List<Hearing> hearing,
                      @JsonAlias("Tribunal_Type_Id") String tribunalTypeId,
                      @JsonAlias("Appeal_Case_NINO") String appealCaseNino,
                      @JsonAlias("Appeal_Case_Id") String appealCaseId,
                      @JsonAlias("Appeal_Case_Major_Id") String appealCaseMajorId,
                      @JsonAlias("Appeal_Case_Case_Code_Id") String appealCaseCaseCodeId,
                      @JsonAlias("Appeal_Case_Date_Appeal_Made") String appealCaseDateAppealMade,
                      @JsonAlias("Appeal_Case_Date_Appeal_Received") String appealCaseDateAppealReceived,
                      @JsonAlias("Postponement_Requests") List<PostponementRequests> postponementRequests,
                      @JsonAlias("Minor_Status") List<MinorStatus> minorStatus,
                      @JsonAlias("Additional_Ref") String additionalRef) {
        this.appealCaseDateOfDecision = appealCaseDateOfDecision;
        this.adminTeamId = adminTeamId;
        this.originatingOfficeId = originatingOfficeId;
        this.appealCaseDateFtaResponseReceived = appealCaseDateFtaResponseReceived;
        this.furtherEvidence = furtherEvidence;
        this.appealCaseRefNum = appealCaseRefNum;
        this.majorStatus = majorStatus;
        this.extractTimeUtc = extractTimeUtc;
        this.appealCaseBfDate = appealCaseBfDate;
        this.parties = parties;
        this.hearing = hearing;
        this.tribunalTypeId = tribunalTypeId;
        this.appealCaseNino = appealCaseNino;
        this.appealCaseId = appealCaseId;
        this.appealCaseMajorId = appealCaseMajorId;
        this.appealCaseCaseCodeId = appealCaseCaseCodeId;
        this.appealCaseDateAppealMade = appealCaseDateAppealMade;
        this.appealCaseDateAppealReceived = appealCaseDateAppealReceived;
        this.postponementRequests = postponementRequests;
        this.minorStatus = minorStatus;
        this.additionalRef = additionalRef;
    }

    @JsonIgnore
    public LocalDate getCreateDate() {
        Optional<MajorStatus> first = majorStatus.stream()
            .filter(ms -> appealRecieved.contains(ms.getStatusId()))
            .findFirst();
        return first.isPresent() ? first.get().getDateSet().toLocalDate() : LocalDate.of(1900, 1, 1);
    }

    public String getAppealCaseNino() {
        return appealCaseNino == null ? "" : appealCaseNino;
    }
}
