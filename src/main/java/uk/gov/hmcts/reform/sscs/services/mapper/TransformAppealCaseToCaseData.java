package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;

@Service
public class TransformAppealCaseToCaseData {

    @Value("${rpc.venue.id.enabled}")
    private boolean lookupRpcByVenueId;

    private final CaseDataBuilder caseDataBuilder;

    @Autowired
    TransformAppealCaseToCaseData(CaseDataBuilder caseDataBuilder) {
        this.caseDataBuilder = caseDataBuilder;
    }

    public SscsCaseData transform(final AppealCase appealCase) {
        List<Parties> parties = appealCase.getParties();

        Optional<Parties> appellantParty = (parties == null) ? Optional.empty() :
            parties.stream().filter(f -> f.getRoleId() == 4).findFirst();

        Optional<Parties> representativeParty = (parties == null) ? Optional.empty() :
            parties.stream().filter(f -> f.getRoleId() == 3).findFirst();

        BenefitType benefitType = caseDataBuilder.buildBenefitType(appealCase);

        Appeal appeal = getAppeal(appealCase, appellantParty, representativeParty, benefitType);

        List<Hearing> hearingsList = caseDataBuilder.buildHearings(appealCase);

        Evidence evidence = caseDataBuilder.buildEvidence(appealCase);

        List<DwpTimeExtension> dwpTimeExtensionList = caseDataBuilder.buildDwpTimeExtensions(appealCase);

        List<Event> events = caseDataBuilder.buildEvent(appealCase);
        RegionalProcessingCenter regionalProcessingCenter = appellantParty.map((Parties party) ->
            regionalProcessingCenter(party, appealCase)).orElse(null);
        return SscsCaseData.builder()
            .caseReference(appealCase.getAppealCaseRefNum())
            .appeal(appeal)
            .hearings(hearingsList)
            .regionalProcessingCenter(regionalProcessingCenter)
            .region((regionalProcessingCenter != null) ? regionalProcessingCenter.getName() : null)
            .evidence(evidence)
            .dwpTimeExtension(dwpTimeExtensionList)
            .events(events)
            .generatedNino(appeal.getAppellant() != null ? appeal.getAppellant().getIdentity().getNino() : null)
            .generatedSurname(appeal.getAppellant() != null ? appeal.getAppellant().getName().getLastName() : null)
            .generatedEmail(appeal.getAppellant() != null ? appeal.getAppellant().getContact().getEmail() : null)
            .generatedMobile(appeal.getAppellant() != null ? appeal.getAppellant().getContact().getMobile() : null)
            .generatedDob(appeal.getAppellant() != null ? appeal.getAppellant().getIdentity().getDob() : null)
            .subscriptions(caseDataBuilder.buildSubscriptions(representativeParty, appealCase.getAppealCaseRefNum()))
            .ccdCaseId(appealCase.getAdditionalRef())
            .build();
    }

    private Appeal getAppeal(final AppealCase appealCase, final Optional<Parties> appellantParty,
                             final Optional<Parties> representativeParty, final BenefitType benefitType) {
        return Appeal.builder()
                .appellant(appellantParty.map((Parties party) -> appellant(party, appealCase)).orElse(null))
                .benefitType(benefitType)
                .hearingOptions(appellantParty.map((Parties party) -> hearingOptions(party, appealCase)).orElse(null))
                .hearingType(HearingType.getHearingTypeByTribunalsTypeId(appealCase.getTribunalTypeId()).getValue())
                .rep(representativeParty.map(this::representative).orElse(null))
                .build();
    }

    private RegionalProcessingCenter regionalProcessingCenter(final Parties appellantParty,
                                                              final AppealCase appealCase) {
        RegionalProcessingCenter regionalProcessingCenter = null;
        if (lookupRpcByVenueId) {
            regionalProcessingCenter = caseDataBuilder.buildRegionalProcessingCentre(appealCase, appellantParty);
        }
        return regionalProcessingCenter;
    }

    private Appellant appellant(final Parties appellantParty, final AppealCase appealCase) {
        return Appellant.builder()
            .name(caseDataBuilder.buildName(appellantParty))
            .contact(caseDataBuilder.buildContact(appellantParty))
            .identity(caseDataBuilder.buildIdentity(appellantParty, appealCase))
            .build();
    }

    private HearingOptions hearingOptions(final Parties appellantParty, final AppealCase appealCase) {
        return caseDataBuilder.buildHearingOptions(appellantParty, appealCase.getTribunalTypeId());
    }

    private Representative representative(final Parties representativeParty) {
        return Representative.builder()
            .hasRepresentative("Yes")
            .contact(caseDataBuilder.buildContact(representativeParty))
            .name(caseDataBuilder.buildName(representativeParty))
            .build();
    }

}
