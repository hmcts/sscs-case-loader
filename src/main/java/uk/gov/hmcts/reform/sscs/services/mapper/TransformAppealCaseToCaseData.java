package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appeal;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appellant;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Contact;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.DwpTimeExtension;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.HearingOptions;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Identity;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Name;

@Service
public class TransformAppealCaseToCaseData {

    private final CaseDataBuilder caseDataBuilder;

    @Autowired
    TransformAppealCaseToCaseData(CaseDataBuilder caseDataBuilder) {
        this.caseDataBuilder = caseDataBuilder;
    }

    public CaseData transform(AppealCase appealCase) {
        List<Parties> parties = appealCase.getParties();

        Optional<Parties> party = (parties == null) ? Optional.empty() :
            parties.stream().filter(f -> f.getRoleId() == 4).findFirst();

        Name name = null;
        Contact contact = null;
        Identity identity = null;
        HearingOptions hearingOptions = null;
        String generatedNino = "";
        String generatedSurname = "";
        String generatedEmail = "";
        String generatedMobile = "";
        Appellant appellant = null;

        if (party.isPresent()) {
            name = caseDataBuilder.buildName(party.get());
            contact = caseDataBuilder.buildContact(party.get());
            identity = caseDataBuilder.buildIdentity(party.get(), appealCase);
            hearingOptions = caseDataBuilder.buildHearingOptions(party.get());
            generatedNino = identity.getNino();
            generatedSurname = name.getLastName();
            generatedEmail = contact.getEmail();
            generatedMobile = contact.getMobile();

            appellant = Appellant.builder()
                .name(name)
                .contact(contact)
                .identity(identity)
                .build();
        }

        BenefitType benefitType = caseDataBuilder.buildBenefitType(appealCase);

        Appeal appeal = Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .hearingOptions(hearingOptions)
            .build();

        List<Hearing> hearingsList = caseDataBuilder.buildHearings(appealCase);
        RegionalProcessingCenter regionalProcessingCenter = caseDataBuilder.buildRegionalProcessingCentre(appealCase);
        String region = (regionalProcessingCenter != null) ? regionalProcessingCenter.getName() : null;

        Evidence evidence = caseDataBuilder.buildEvidence(appealCase);

        List<DwpTimeExtension> dwpTimeExtensionList = caseDataBuilder.buildDwpTimeExtensions(appealCase);

        List<Events> events = caseDataBuilder.buildEvent(appealCase);
        return CaseData.builder()
            .caseReference(appealCase.getAppealCaseRefNum())
            .appeal(appeal)
            .hearings(hearingsList)
            .regionalProcessingCenter(regionalProcessingCenter)
            .region(region)
            .evidence(evidence)
            .dwpTimeExtension(dwpTimeExtensionList)
            .events(events)
            .generatedNino(generatedNino)
            .generatedSurname(generatedSurname)
            .generatedEmail(generatedEmail)
            .generatedMobile(generatedMobile)
            .subscriptions(caseDataBuilder.buildSubscriptions())
            .ccdCaseId(appealCase.getAdditionalRef())
            .build();
    }

}
