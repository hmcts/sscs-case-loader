package uk.gov.hmcts.reform.tools.builders;

import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.reform.tools.enums.PartyPPTP;
import uk.gov.hmcts.reform.tools.enums.PartyType;
import uk.gov.hmcts.reform.tools.utils.XMLTags;

public class AppealParties {

    private Map<String,String> appealParties = new HashMap<>();

    public AppealParties setPTTP_Id(PartyPPTP partyPPTP) {
        appealParties.put(XMLTags.pttp_id, partyPPTP.getCode());
        return this;
    }

    public AppealParties setTitle(String value){
        appealParties.put(XMLTags.title, value);
        return this;
    }

    public AppealParties setInitials(String value){
        appealParties.put(XMLTags.initials, value);
        return this;
    }

    public AppealParties setSurname(String value){
        appealParties.put(XMLTags.surname, value);
        return this;
    }

    public AppealParties setPostcode(String value){
        appealParties.put(XMLTags.postcode, value);
        return this;
    }
    public AppealParties setRoleId(PartyType appellantType){
        appealParties.put(XMLTags.roleId, appellantType.getCode());
        return this;
    }

    public AppealParties setAttending(String value){
        appealParties.put(XMLTags.attending, value);
        return this;
    }

    public AppealParties setDisabilityNeeds(String value){
        appealParties.put(XMLTags.disabilityNeeds, value);
        return this;
    }

    public Map<String, String> build() {

        return appealParties;
    }

}
