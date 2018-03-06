package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Parties {
    private String initials;
    private String title;
    private String surname;
    private String email;
    private String phone1;
    private String phone2;
    private String postCode;
    private String disabilityNeeds;
    private String interpreterSignerId;
    private String dob;
    private Integer roleId;

    public Parties(@JsonProperty("INITIALS") String initials,
            @JsonProperty("Title") String title,
            @JsonProperty("Surname") String surname,
            @JsonProperty("Email") String email,
            @JsonProperty("Phone_1") String phone1,
            @JsonProperty("Phone_2") String phone2,
            @JsonProperty("Postcode") String postCode,
            @JsonProperty("Disability_Needs") String disabilityNeeds,
            @JsonProperty("Interpreter_Signer_Id") String interpreterSignerId,
            @JsonProperty("DOB") String dob,
            @JsonProperty("Role_Id") Integer roleId) {
        this.initials = initials;
        this.title = title;
        this.surname = surname;
        this.email = email;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.postCode = postCode;
        this.disabilityNeeds = disabilityNeeds;
        this.interpreterSignerId = interpreterSignerId;
        this.dob = dob;
        this.roleId = roleId;
    }
}
