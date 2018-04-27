package uk.gov.hmcts.reform.tools.builders;

import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.reform.tools.enums.PartyPptp;
import uk.gov.hmcts.reform.tools.enums.PartyType;
import uk.gov.hmcts.reform.tools.utils.XmlTags;

public class AppealParties {

    private Map<String,String> appealParties = new HashMap<>();

    public AppealParties setPttp_Id(PartyPptp partyPptp) {
        appealParties.put(XmlTags.pttp_id, partyPptp.getCode());
        return this;
    }

    public AppealParties setTitle(String value) {
        appealParties.put(XmlTags.title, value);
        return this;
    }

    public AppealParties setInitials(String value) {
        appealParties.put(XmlTags.initials, value);
        return this;
    }

    public AppealParties setSurname(String value) {
        appealParties.put(XmlTags.surname, value);
        return this;
    }

    public AppealParties setPostcode(String value) {
        appealParties.put(XmlTags.postcode, value);
        return this;
    }

    public AppealParties setRoleId(PartyType appellantType) {
        appealParties.put(XmlTags.roleId, appellantType.getCode());
        return this;
    }

    public AppealParties setAttending(String value) {
        appealParties.put(XmlTags.attending, value);
        return this;
    }

    public AppealParties setDisabilityNeeds(String value) {
        appealParties.put(XmlTags.disabilityNeeds, value);
        return this;
    }

    public Map<String, String> build() {

        return appealParties;
    }

}
