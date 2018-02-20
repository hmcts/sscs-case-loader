package uk.gov.hmcts.reform.sscs.services.refdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sscs.refdataloaders.GoogleMapRefDataLoader;

@Service
public class ReferenceDataService {

    private final GoogleMapRefDataLoader googleMapRefDataLoader;

    @Autowired
    public ReferenceDataService(GoogleMapRefDataLoader googleMapRefDataLoader) {
        this.googleMapRefDataLoader = googleMapRefDataLoader;
    }

    public String getGoogleMapUrl(String venueId) {
        return googleMapRefDataLoader.getVenueIdGoogleUrlmap().get(venueId);
    }
}
