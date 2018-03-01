package uk.gov.hmcts.reform.sscs.services.refdata;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.refdata.ReferenceDataLoader;

@Service
public class ReferenceDataService {

    private final ReferenceDataLoader referenceDataLoader;

    @Autowired
    public ReferenceDataService(ReferenceDataLoader referenceDataLoader) {
        this.referenceDataLoader = referenceDataLoader;
    }

    public VenueDetails getVenueDetails(String venueId) {
        Stream<VenueDetails> venueDetailsStream = referenceDataLoader.getVenueDetailsList()
            .stream().filter(
                venueDetails ->
                    venueId != null && !("".equals(venueId)) && venueId.equals(venueDetails.getVenueId())
                );
        Optional<VenueDetails> optionalVenueDetails = venueDetailsStream.findAny();
        return optionalVenueDetails.orElse(null);
    }
}
