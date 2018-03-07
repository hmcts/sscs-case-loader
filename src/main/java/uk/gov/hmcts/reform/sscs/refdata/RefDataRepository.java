package uk.gov.hmcts.reform.sscs.refdata;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKey;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField;

@Component
public class RefDataRepository {

    private final Map<RefKey, Map<String, Map<RefKeyField, String>>> dataMap = newHashMap();

    public String find(RefKey key, String value, RefKeyField keyField) {
        return dataMap.get(key).get(value).get(keyField);
    }

    public void add(RefKey key, String keyId, RefKeyField keyField, String fieldValue) {
        Map<String, Map<RefKeyField, String>> valueMap = dataMap.computeIfAbsent(key, k -> newHashMap());
        Map<RefKeyField, String> refDataMap = valueMap.computeIfAbsent(keyId, v -> newHashMap());
        refDataMap.put(keyField, fieldValue);
    }
}
