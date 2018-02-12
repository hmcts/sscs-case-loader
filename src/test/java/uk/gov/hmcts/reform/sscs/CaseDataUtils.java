package uk.gov.hmcts.reform.sscs;

import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;

public final class CaseDataUtils {
    private CaseDataUtils() {
    }

    public static CaseData buildCaseData() {
        Name name = Name.builder()
            .title("Mr")
            .firstName("Userzzz")
            .lastName("Test")
            .build();
        Address address = Address.builder()
            .postcode("L17 7AE")
            .build();
        Contact contact = Contact.builder()
            .email("mail@email.com")
            .phone("01234567890")
            .mobile("01234567890")
            .build();
        Identity identity = Identity.builder()
            .dob("1904-03-10")
            .nino("AB 22 55 66 B")
            .build();
        Appellant appellant = Appellant.builder()
            .name(name)
            .address(address)
            .contact(contact)
            .identity(identity)
            .build();
        Appeal appeal = Appeal.builder()
            .mrnDate("2017-10-08")
            .mrnMissingReason("It was missing")
            .appellant(appellant)
            .build();
        return CaseData.builder()
            .caseReference("SC068/17/00013")
            .appeal(appeal).build();
    }
}
