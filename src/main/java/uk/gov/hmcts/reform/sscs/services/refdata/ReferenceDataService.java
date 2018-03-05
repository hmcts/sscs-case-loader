package uk.gov.hmcts.reform.sscs.services.refdata;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.refdata.VenueDataLoader;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKey;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField;

@Service
public class ReferenceDataService {


    private final Map<String, VenueDetails> venueDataMap;
    private RefDataRepository refDataRepo;

    @Autowired
    public ReferenceDataService(VenueDataLoader venueDataLoader, RefDataRepository refDataRepo) {
        this.venueDataMap = venueDataLoader.getVenueDetailsMap();
        this.refDataRepo = refDataRepo;
    }

    public VenueDetails getVenueDetails(String venueId) {
        return venueDataMap.get(venueId);
    }

    public String getRefField(RefKey key, RefKeyField field) {
        return refDataRepo.find(key, field);
    }
}
