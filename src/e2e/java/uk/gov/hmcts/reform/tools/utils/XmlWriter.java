package uk.gov.hmcts.reform.tools.utils;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.hmcts.reform.tools.builders.Appeal;
import uk.gov.hmcts.reform.tools.enums.OutcomeType;

public class XmlWriter {

    private final Document doc;
    private String path;
    private Document preUpdate;
    private Element appealsCollection;
    private Element appealCollection;
    private Element majorStatusCollection;
    private Element partiesCollection;
    private Element hearingCollection;
    private final List<Appeal> appealsToBeWritten = new ArrayList<>();

    Element schemaVersion;
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private final Date lastUpdatedDate = new Date();

    public XmlWriter(Document doc) throws ParserConfigurationException {
        this.doc = doc;
        setAppealCollection(); //NOPMD
    }

    public XmlWriter(String path) throws ParserConfigurationException {
        this.path = path;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();
        setRoot(); //NOPMD
    }

    public XmlWriter newXmlWriter() {
        return this;
    }

    public XmlWriter setRoot() {
        appealsCollection = doc.createElement("Appeal_Cases");
        doc.appendChild(appealsCollection);
        return this;
    }

    public XmlWriter setAppealCollection() {
        appealCollection = doc.createElement("Appeal_Case");
        return this;
    }

    public XmlWriter setAppealHeaderElement(String elementName, String value) {
        Element headerElement = doc.createElement(elementName);
        headerElement.appendChild(doc.createTextNode(value));
        appealCollection.appendChild(headerElement);
        return this;
    }

    public XmlWriter setHeader(Map<String, String> headers) {
        setAppealHeaderElement(XmlTags.extract_Time_UTC, headers.get(XmlTags.extract_Time_UTC));
        setAppealHeaderElement(XmlTags.appeal_Case_Id, headers.get(XmlTags.appeal_Case_Id));
        setAppealHeaderElement(XmlTags.appeal_Case_RefNum, headers.get(XmlTags.appeal_Case_RefNum));
        setAppealHeaderElement(XmlTags.appeal_Case_Case_Code_Id, headers.get(XmlTags.appeal_Case_Case_Code_Id));
        setAppealHeaderElement(XmlTags.tribunal_Type_Id, headers.get(XmlTags.tribunal_Type_Id));
        setAppealHeaderElement(XmlTags.appeal_Case_Date_Appeal_Received,
                headers.get(XmlTags.appeal_Case_Date_Appeal_Received));
        setAppealHeaderElement(XmlTags.appeal_Case_Date_of_Decision, headers.get(XmlTags.appeal_Case_Date_of_Decision));
        setAppealHeaderElement(XmlTags.appeal_Case_Date_Appeal_Made, headers.get(XmlTags.appeal_Case_Date_Appeal_Made));
        setAppealHeaderElement(XmlTags.appeal_Case_NINO, headers.get(XmlTags.appeal_Case_NINO));
        setAppealHeaderElement(XmlTags.appeal_Case_Major_Id, headers.get(XmlTags.appeal_Case_Major_Id));
        setAppealHeaderElement(XmlTags.appeal_Case_BF_Date, headers.get(XmlTags.appeal_Case_BF_Date));
        setAppealHeaderElement(XmlTags.originating_Office_Id, headers.get(XmlTags.originating_Office_Id));
        setAppealHeaderElement(XmlTags.admin_Team_Id, headers.get(XmlTags.admin_Team_Id));
        return this;
    }

    public XmlWriter setParties(List<Map<String, String>> parties) {

        for (Map<String, String> party : parties) {

            createPartiesCollection();
            setPartiesElement(XmlTags.pttp_id, party.get(XmlTags.pttp_id));
            setPartiesElement(XmlTags.title, party.get(XmlTags.title));
            setPartiesElement(XmlTags.initials, party.get(XmlTags.initials));
            setPartiesElement(XmlTags.surname, party.get(XmlTags.surname));
            setPartiesElement(XmlTags.postcode, party.get(XmlTags.postcode));
            setPartiesElement(XmlTags.roleId, party.get(XmlTags.roleId));
            setPartiesElement(XmlTags.attending, party.get(XmlTags.attending));
            setPartiesElement(XmlTags.disabilityNeeds, party.get(XmlTags.disabilityNeeds));
            addPartiesCollection();

        }

        return this;
    }

    public XmlWriter setHearing(List<Map<String, String>> hearings) {

        if (!hearings.isEmpty()) {

            for (Map<String, String> hearing : hearings) {

                createHearingCollection();

                setHearingElement(XmlTags.hearing_Id, hearing.get(XmlTags.hearing_Id));
                setHearingElement(XmlTags.date_hearing_notification, hearing.get(XmlTags.date_hearing_notification));
                setHearingElement(XmlTags.date_outcome_decision_notification,
                        hearing.get(XmlTags.date_outcome_decision_notification));
                setHearingElement(XmlTags.session_date, hearing.get(XmlTags.session_date));
                setHearingElement(XmlTags.appeal_time, hearing.get(XmlTags.appeal_time));
                setHearingElement(XmlTags.venue_id, hearing.get(XmlTags.venue_id));

                if (OutcomeType.UNSET != OutcomeType.valueOf(hearing.get(XmlTags.outcome_id))) {
                    setHearingElement(XmlTags.outcome_id, hearing.get(XmlTags.outcome_id));
                }

                addHearingCollection();
            }
        }
        return this;

    }

    public XmlWriter setMajorStatuses(List<Map<String, String>> majorstatuses) {

        int i = 0;

        for (Map<String, String> majorstatus : majorstatuses) {

            i++;

            createMajorStatusCollection();
            addMajorStatusElement(XmlTags.status_Id, majorstatus.get(XmlTags.status_Id));
            addMajorStatusElement(XmlTags.date_Set, majorstatus.get(XmlTags.date_Set));

            if (i != majorstatuses.size()) {

                addMajorStatusElement(XmlTags.date_Closed, majorstatus.get(XmlTags.date_Closed));

            }

            addMajorStatusElement(XmlTags.bf_Date, majorstatus.get(XmlTags.bf_Date));

            addMajorStatusCollection();

        }

        return this;

    }

    public XmlWriter createMajorStatusCollection() {
        majorStatusCollection = doc.createElement("Major_Status");
        return this;
    }

    public XmlWriter createPartiesCollection() {
        partiesCollection = doc.createElement("Parties");
        return this;
    }

    public XmlWriter createHearingCollection() {
        hearingCollection = doc.createElement("Hearing");
        return this;
    }

    public XmlWriter addMajorStatusElement(String elementName, String value) {
        Element majorStatusElement = doc.createElement(elementName);
        majorStatusElement.appendChild(doc.createTextNode(value));
        majorStatusCollection.appendChild(majorStatusElement);
        return this;
    }

    public XmlWriter setPartiesElement(String elementName, String value) {
        Element partiesElement = doc.createElement(elementName);
        partiesElement.appendChild(doc.createTextNode(value));
        partiesCollection.appendChild(partiesElement);
        return this;
    }

    public XmlWriter setHearingElement(String elementName, String value) {
        Element hearingElement = doc.createElement(elementName);
        hearingElement.appendChild(doc.createTextNode(value));
        hearingCollection.appendChild(hearingElement);
        return this;
    }

    public XmlWriter addMajorStatusCollection() {
        appealCollection.appendChild(majorStatusCollection);
        return this;
    }

    public XmlWriter addPartiesCollection() {
        appealCollection.appendChild(partiesCollection);
        return this;
    }

    public XmlWriter addHearingCollection() {
        appealCollection.appendChild(hearingCollection);
        return this;
    }

    public XmlWriter pushAppealToBeWritten(Appeal appeal) {
        final Appeal appealToBeWritten = appeal;
        appealsToBeWritten.add(appealToBeWritten);
        return this;
    }

    public List<Appeal> getAppealWrittenToFile() {
        return appealsToBeWritten;
    }

    public XmlWriter addRecordsCollection() {

        Element numOfCases = doc.createElement("NumberOfCases");
        Integer records = appealsToBeWritten.size();
        numOfCases.appendChild(doc.createTextNode(String.valueOf(records)));

        Element recordsCollection = doc.createElement("Num_Records");
        recordsCollection.appendChild(numOfCases);
        appealsCollection.appendChild(recordsCollection);
        return this;

    }

    public XmlWriter addSchemaVersionCollection() {

        return this;
    }


    public XmlWriter writeXml(String ouputdir) throws
            TransformerException, IOException, ParserConfigurationException {

        String dateTime = now().format(ofPattern("yyyy-MM-dd-HH-mm-ss"));
        String fileName = "SSCS_" + path + "_Delta_" + dateTime + ".xml";
        Path path = get(ouputdir + "/" + fileName);

        if (path.getParent() != null) {
            createDirectories(path.getParent());
        }

        writeDoc(path.toString(), doc);

        return this;

    }

    private XmlWriter writeDoc(String path, Document doc) throws
            TransformerException, IOException, ParserConfigurationException {

        for (Appeal appeal : appealsToBeWritten) {
            appeal.write();
            appealsCollection.appendChild(appealCollection);
        }

        addRecordsCollection();
        addSchemaVersionCollection();

        Transformer transformer = transformerFactory.newTransformer();

        Properties properties = new Properties();
        properties.setProperty(OutputKeys.ENCODING, "UTF-8");
        properties.setProperty(OutputKeys.INDENT, "yes");

        transformer.setOutputProperties(properties);

        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new File(path));
        transformer.transform(source, result);
        System.out.println("File saved!");

        return this;

    }

}
