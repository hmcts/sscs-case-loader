package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import static com.google.common.collect.Lists.newArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.Value;

@Value
public class AppealCase {

    private static List<String> appealRecieved = newArrayList(
        "3", "4", "9",
        "11", "14", "86",
        "93", "187", "204",
        "401", "600", "602",
        "603", "604", "605", "608");

    private String appealCaseDateOfDecision;
    private String adminTeamId;
    private String originatingOfficeId;
    private String appealCaseDateFtaResponseReceived;
    private List<FurtherEvidence> furtherEvidence;
    private String appealCaseRefNum;
    private List<MajorStatus> majorStatus;
    private String extractTimeUtc;
    private String appealCaseBfDate;
    private List<Parties> parties;
    private List<Hearing> hearing;
    private String tribunalTypeId;
    private String appealCaseNino;
    private String appealCaseId;
    private String appealCaseMajorId;
    private String appealCaseCaseCodeId;
    private String appealCaseDateAppealMade;
    private String appealCaseDateAppealReceived;
    private List<PostponementRequests> postponementRequests;

    public AppealCase(@JsonProperty("Appeal_Case_Date_of_Decision") String appealCaseDateOfDecision,
                      @JsonProperty("Admin_Team_Id") String adminTeamId,
                      @JsonProperty("Originating_Office_Id") String originatingOfficeId,
                      @JsonProperty("Appeal_Case_Date_FTA_Response_Received") String appealCaseDateFtaResponseReceived,
                      @JsonProperty("Further_Evidence") List<FurtherEvidence> furtherEvidence,
                      @JsonProperty("Appeal_Case_RefNum") String appealCaseRefNum,
                      @JsonProperty("Major_Status") List<MajorStatus> majorStatus,
                      @JsonProperty("Extract_Time_UTC") String extractTimeUtc,
                      @JsonProperty("Appeal_Case_BF_Date") String appealCaseBfDate,
                      @JsonProperty("Parties") List<Parties> parties,
                      @JsonProperty("Hearing") List<Hearing> hearing,
                      @JsonProperty("Tribunal_Type_Id") String tribunalTypeId,
                      @JsonProperty("Appeal_Case_NINO") String appealCaseNino,
                      @JsonProperty("Appeal_Case_Id") String appealCaseId,
                      @JsonProperty("Appeal_Case_Major_Id") String appealCaseMajorId,
                      @JsonProperty("Appeal_Case_Case_Code_Id") String appealCaseCaseCodeId,
                      @JsonProperty("Appeal_Case_Date_Appeal_Made") String appealCaseDateAppealMade,
                      @JsonProperty("Appeal_Case_Date_Appeal_Received") String appealCaseDateAppealReceived,
                      @JsonProperty("Postponement_Requests") List<PostponementRequests> postponementRequests) {
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
    }

    @JsonIgnore
    public LocalDate getCreateDate() {
        Optional<MajorStatus> first = majorStatus.stream()
            .filter(ms -> appealRecieved.contains(ms.getStatusId()))
            .findFirst();
        return first.isPresent() ? first.get().getDateSet().toLocalDate() : LocalDate.of(1900, 01, 01);
    }
}
