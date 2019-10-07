package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

@Slf4j
@Service
class UpdateCcdRpc {
    boolean updateCcdRpc(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        boolean rpcUpdated = false;

        if (gapsCaseData == null
            || existingCcdCaseData == null
            || gapsCaseData.getRegionalProcessingCenter() == null) {
            return false;
        }

        RegionalProcessingCenter gapsRpc = gapsCaseData.getRegionalProcessingCenter();
        RegionalProcessingCenter existingRpc = existingCcdCaseData.getRegionalProcessingCenter();
        if (hasRpcChanged(gapsRpc, existingRpc)) {
            existingCcdCaseData.setRegionalProcessingCenter(gapsRpc);
            existingCcdCaseData.setRegion(gapsRpc.getName());
            rpcUpdated = true;
        }

        return rpcUpdated;
    }


    private static boolean hasRpcChanged(RegionalProcessingCenter gapsRpc, RegionalProcessingCenter existingRpc) {
        return existingRpc == null
            || existingRpc.getName() == null
            || existingRpc.getAddress1() == null
            || gapsRpc.getName() == null
            || gapsRpc.getAddress1() == null
            || !existingRpc.getName().equals(gapsRpc.getName())
            || !existingRpc.getAddress1().equals(gapsRpc.getAddress1());
    }

}
