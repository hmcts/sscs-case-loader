package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Address;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appeal;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appellant;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Contact;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.DwpTimeExtension;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.HearingOptions;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Identity;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Name;

@Service
public class TransformAppealCaseToCaseData {

    private final CaseDataBuilder caseDataBuilder;

    @Autowired
    public TransformAppealCaseToCaseData(CaseDataBuilder caseDataBuilder) {
        this.caseDataBuilder = caseDataBuilder;
    }

    public CaseData transform(AppealCase appealCase) {
        Name name = caseDataBuilder.buildName(appealCase);
        Address address = caseDataBuilder.buildAddress(appealCase);
        Contact contact = caseDataBuilder.buildContact(appealCase);
        Identity identity = caseDataBuilder.buildIdentity(appealCase);

        Appellant appellant = Appellant.builder()
            .name(name)
            .address(address)
            .contact(contact)
            .identity(identity)
            .build();

        BenefitType benefitType = caseDataBuilder.buildBenefitType(appealCase);

        HearingOptions hearingOptions = caseDataBuilder.buildHearingOptions(appealCase);

        Appeal appeal = Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .hearingOptions(hearingOptions)
            .build();

        List<Hearing> hearingsList = caseDataBuilder.buildHearings(appealCase);

        Evidence evidence = caseDataBuilder.buildEvidence(appealCase);

        List<DwpTimeExtension> dwpTimeExtensionList = caseDataBuilder.buildDwpTimeExtensions(appealCase);

        return CaseData.builder()
            .caseReference(appealCase.getAppealCaseRefNum())
            .appeal(appeal)
            .hearings(hearingsList)
            .evidence(evidence)
            .dwpTimeExtension(dwpTimeExtensionList)
            .events(caseDataBuilder.buildEvents(appealCase))
            .build();
    }

}
