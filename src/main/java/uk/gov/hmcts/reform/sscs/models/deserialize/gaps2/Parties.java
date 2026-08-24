package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Parties {

    String initials;
    String title;
    String surname;
    String email;
    String phone1;
    String phone2;
    String postCode;
    String disabilityNeeds;
    String interpreterSignerId;
    String dob;
    Integer roleId;

    public Parties(@JsonAlias("INITIALS") String initials,
                   @JsonAlias("Title") String title,
                   @JsonAlias("Surname") String surname,
                   @JsonAlias("Email") String email,
                   @JsonAlias("Phone_1") String phone1,
                   @JsonAlias("Phone_2") String phone2,
                   @JsonAlias("Postcode") String postCode,
                   @JsonAlias("Disability_Needs") String disabilityNeeds,
                   @JsonAlias("Interpreter_Signer_Id") String interpreterSignerId,
                   @JsonAlias("DOB") String dob,
                   @JsonAlias("Role_Id") Integer roleId) {
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

    public String getMobile() {
        return phone1;
    }

    public String getLandline() {
        return phone2;
    }
}
