package uk.gov.hmcts.reform.sscs.services.mapper;

import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

@Service
public class TransformXmlFilesToJsonFiles {
    public JsonFiles transform(XmlFiles xmlFiles) {
        JSONObject jsonDelta = XML.toJSONObject(xmlFiles.getDelta());
        JSONObject jsonRef = XML.toJSONObject(String.valueOf(xmlFiles.getRef()));
        return JsonFiles.builder().delta(jsonDelta).ref(jsonRef).build();
    }

}
