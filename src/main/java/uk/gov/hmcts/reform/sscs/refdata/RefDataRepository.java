package uk.gov.hmcts.reform.sscs.refdata;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKey;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField;

@Component
public class RefDataRepository {

    private final Map<String, String> dataMap = newHashMap();

    public String find(RefKey key, RefKeyField keyField) {
        return dataMap.get(getKey(key.name(), keyField.name()));
    }

    public void add(String key, String keyField, String value) {
        dataMap.put(getKey(key, keyField), value);
    }

    private String getKey(String key, String keyField) {
        return String.format("%s|%s", key, keyField);
    }
}
