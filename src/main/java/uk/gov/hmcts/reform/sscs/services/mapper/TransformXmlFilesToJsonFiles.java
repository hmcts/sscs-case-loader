package uk.gov.hmcts.reform.sscs.services.mapper;

import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;

@Service
public class TransformXmlFilesToJsonFiles {
    public JSONObject transform(String input) {
        return XML.toJSONObject(input);
    }

}
