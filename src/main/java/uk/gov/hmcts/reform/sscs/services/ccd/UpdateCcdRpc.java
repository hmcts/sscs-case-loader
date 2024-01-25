package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.exceptions.FeignExceptionLogger.debugCaseLoaderException;

import feign.FeignException;
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

            try {
                log.info("RPC has changed from {} to {} for case {}", toName(existingRpc), toName(gapsRpc),
                    existingCcdCaseData.getCcdCaseId());

                existingCcdCaseData.setRegionalProcessingCenter(gapsRpc);
                existingCcdCaseData.setRegion(gapsRpc.getName());

                String isScottishCase = isScottishCase(gapsRpc, existingCcdCaseData);

                existingCcdCaseData.setIsScottishCase(isScottishCase);

                rpcUpdated = true;
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Regional Processing Center");
            }
        } else {
            log.info("RPC has not changed for case {} . RPC =  {}", existingCcdCaseData.getCcdCaseId(),
                toName(existingRpc));
        }

        return rpcUpdated;
    }

    private String toName(RegionalProcessingCenter regionalProcessingCenter) {
        return regionalProcessingCenter == null || regionalProcessingCenter.getName() == null ? "None" :
            regionalProcessingCenter.getName();
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

    public static String isScottishCase(RegionalProcessingCenter rpc, SscsCaseData caseData) {

        if (isNull(rpc) || isNull(rpc.getName())) {
            log.info("Setting isScottishCase field to No for empty RPC for case " + caseData.getCcdCaseId());
            return "No";
        } else {
            String isScotCase = rpc.getName().equalsIgnoreCase("GLASGOW") ? "Yes" : "No";
            log.info("Setting isScottishCase field to " + isScotCase + " for RPC " + rpc.getName() + " for case "
                + caseData.getCcdCaseId());
            return isScotCase;
        }
    }

}
