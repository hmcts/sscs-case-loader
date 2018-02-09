package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

import lombok.Value;

@Value
public class AppealCase {
    private String appealCaseDateOfDecision;
    private String adminTeamId;
    private String originatingOfficeId;
    private String appealCaseDateFtaResponseReceived;
    private String appealCaseRefNum;
    private ArrayList<MajorStatus> majorStatus;
    private String extractTimeUtc;
    private String appealCaseBfDate;
    private Parties parties;
    private Hearing hearing;
    private String tribunalTypeId;
    private String appealCaseNino;
    private String appealCaseId;
    private String appealCaseMajorId;
    private String appealCaseCaseCodeId;
    private String appealCaseDateAppealMade;
    private String appealCaseDateAppealReceived;
    private PostponementRequests postponementRequests;


    public AppealCase(@JsonProperty("Appeal_Case_Date_of_Decision") String appealCaseDateOfDecision,
                      @JsonProperty("Admin_Team_Id") String adminTeamId,
                      @JsonProperty("Originating_Office_Id") String originatingOfficeId,
                      @JsonProperty("Appeal_Case_Date_FTA_Response_Received") String appealCaseDateFtaResponseReceived,
                      @JsonProperty("Appeal_Case_RefNum") String appealCaseRefNum,
                      @JsonProperty("Major_Status") ArrayList<MajorStatus> majorStatus,
                      @JsonProperty("Extract_Time_UTC") String extractTimeUtc,
                      @JsonProperty("Appeal_Case_BF_Date") String appealCaseBfDate,
                      @JsonProperty("Parties") Parties parties,
                      @JsonProperty("Hearing") Hearing hearing,
                      @JsonProperty("Tribunal_Type_Id") String tribunalTypeId,
                      @JsonProperty("Appeal_Case_NINO") String appealCaseNino,
                      @JsonProperty("Appeal_Case_Id") String appealCaseId,
                      @JsonProperty("Appeal_Case_Major_Id") String appealCaseMajorId,
                      @JsonProperty("Appeal_Case_Case_Code_Id") String appealCaseCaseCodeId,
                      @JsonProperty("Appeal_Case_Date_Appeal_Made") String appealCaseDateAppealMade,
                      @JsonProperty("Appeal_Case_Date_Appeal_Received") String appealCaseDateAppealReceived,
                      @JsonProperty("Postponement_Requests") PostponementRequests postponementRequests) {
        this.appealCaseDateOfDecision = appealCaseDateOfDecision;
        this.adminTeamId = adminTeamId;
        this.originatingOfficeId = originatingOfficeId;
        this.appealCaseDateFtaResponseReceived = appealCaseDateFtaResponseReceived;
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
}
