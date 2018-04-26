package uk.gov.hmcts.reform.tools.utils;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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

public class XMLWriter {

    private Document doc;
    private String path;
    private Document preUpdate;
    private Element appealsCollection;
    private Element appealCollection;
    private Element majorStatusCollection;
    private Element partiesCollection;
    private Element hearingCollection;
    private List<Appeal> appealsToBeWritten = new ArrayList<>();

    Element schemaVersion;
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private Date lastUpdatedDate = new Date();

    public XMLWriter(Document doc) throws ParserConfigurationException {
        this.doc = doc;
        setAppealCollection();
    }

    public XMLWriter(String path) throws ParserConfigurationException {
        this.path = path;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();
        setRoot();
    }

    public XMLWriter newXMLWriter(){
        return this;
    }

    public XMLWriter setRoot(){
        appealsCollection = doc.createElement("Appeal_Cases");
        doc.appendChild(appealsCollection);
        return this;
    }

    public XMLWriter setAppealCollection(){
        appealCollection = doc.createElement("Appeal_Case");
        return this;
    }

    public XMLWriter setAppealHeaderElement(String elementName, String value){
        Element headerElement = doc.createElement(elementName);
        headerElement.appendChild(doc.createTextNode(value));
        appealCollection.appendChild(headerElement);
        return this;
    }

    public XMLWriter setHeader(Map<String, String> headers) {
        setAppealHeaderElement(XMLTags.extract_Time_UTC, headers.get(XMLTags.extract_Time_UTC));
        setAppealHeaderElement(XMLTags.appeal_Case_Id, headers.get(XMLTags.appeal_Case_Id));
        setAppealHeaderElement(XMLTags.appeal_Case_RefNum, headers.get(XMLTags.appeal_Case_RefNum));
        setAppealHeaderElement(XMLTags.appeal_Case_Case_Code_Id, headers.get(XMLTags.appeal_Case_Case_Code_Id));
        setAppealHeaderElement(XMLTags.tribunal_Type_Id, headers.get(XMLTags.tribunal_Type_Id));
        setAppealHeaderElement(XMLTags.appeal_Case_Date_Appeal_Received, headers.get(XMLTags.appeal_Case_Date_Appeal_Received));
        setAppealHeaderElement(XMLTags.appeal_Case_Date_of_Decision, headers.get(XMLTags.appeal_Case_Date_of_Decision));
        setAppealHeaderElement(XMLTags.appeal_Case_Date_Appeal_Made, headers.get(XMLTags.appeal_Case_Date_Appeal_Made));
        setAppealHeaderElement(XMLTags.appeal_Case_NINO, headers.get(XMLTags.appeal_Case_NINO));
        setAppealHeaderElement(XMLTags.appeal_Case_Major_Id, headers.get(XMLTags.appeal_Case_Major_Id));
        setAppealHeaderElement(XMLTags.appeal_Case_BF_Date, headers.get(XMLTags.appeal_Case_BF_Date));
        setAppealHeaderElement(XMLTags.originating_Office_Id, headers.get(XMLTags.originating_Office_Id));
        setAppealHeaderElement(XMLTags.admin_Team_Id, headers.get(XMLTags.admin_Team_Id));
        return this;
    }

    public XMLWriter setParties(List<Map<String, String>> parties) {

        for(Map<String, String> party : parties){

            createPartiesCollection();
            setPartiesElement(XMLTags.pttp_id, party.get(XMLTags.pttp_id));
            setPartiesElement(XMLTags.title, party.get(XMLTags.title));
            setPartiesElement(XMLTags.initials, party.get(XMLTags.initials));
            setPartiesElement(XMLTags.surname, party.get(XMLTags.surname));
            setPartiesElement(XMLTags.postcode, party.get(XMLTags.postcode));
            setPartiesElement(XMLTags.roleId, party.get(XMLTags.roleId));
            setPartiesElement(XMLTags.attending, party.get(XMLTags.attending));
            setPartiesElement(XMLTags.disabilityNeeds, party.get(XMLTags.disabilityNeeds));
            addPartiesCollection();

        }

        return this;
    }

    public XMLWriter setHearing(List<Map<String, String>> hearings) {

        if (!hearings.isEmpty()) {

            for (Map<String, String> hearing : hearings) {

                createHearingCollection();

                setHearingElement(XMLTags.hearing_Id, hearing.get(XMLTags.hearing_Id));
                setHearingElement(XMLTags.date_hearing_notification, hearing.get(XMLTags.date_hearing_notification));
                setHearingElement(XMLTags.date_outcome_decision_notification, hearing.get(XMLTags.date_outcome_decision_notification));
                setHearingElement(XMLTags.session_date, hearing.get(XMLTags.session_date));
                setHearingElement(XMLTags.appeal_time, hearing.get(XMLTags.appeal_time));
                setHearingElement(XMLTags.venue_id, hearing.get(XMLTags.venue_id));

                if (OutcomeType.UNSET != OutcomeType.valueOf(hearing.get(XMLTags.outcome_id))) {
                    setHearingElement(XMLTags.outcome_id, hearing.get(XMLTags.outcome_id));
                }

                addHearingCollection();
            }
        }
        return this;

    }
    public XMLWriter setMajorStatuses(List<Map<String, String>> major_statuses) {

        int i = 0;

        for(Map<String, String> major_status : major_statuses){

            i++;

            createMajorStatusCollection();
            addMajorStatusElement(XMLTags.status_Id, major_status.get(XMLTags.status_Id));
            addMajorStatusElement(XMLTags.date_Set, major_status.get(XMLTags.date_Set));

            if (i!=major_statuses.size()) {

                addMajorStatusElement(XMLTags.date_Closed, major_status.get(XMLTags.date_Closed));

            }

            addMajorStatusElement(XMLTags.bf_Date, major_status.get(XMLTags.bf_Date));

            addMajorStatusCollection();

        }

        return this;

    }

    public XMLWriter createMajorStatusCollection(){
        majorStatusCollection = doc.createElement("Major_Status");
        return this;
    }

    public XMLWriter createPartiesCollection(){
        partiesCollection = doc.createElement("Parties");
        return this;
    }

    public XMLWriter createHearingCollection(){
        hearingCollection = doc.createElement("Hearing");
        return this;
    }

    public XMLWriter addMajorStatusElement(String elementName, String value) {
        Element majorStatusElement = doc.createElement(elementName);
        majorStatusElement.appendChild(doc.createTextNode(value));
        majorStatusCollection.appendChild(majorStatusElement);
        return this;
    }

    public XMLWriter setPartiesElement(String elementName, String value) {
        Element partiesElement = doc.createElement(elementName);
        partiesElement.appendChild(doc.createTextNode(value));
        partiesCollection.appendChild(partiesElement);
        return this;
    }

    public XMLWriter setHearingElement(String elementName, String value) {
        Element hearingElement = doc.createElement(elementName);
        hearingElement.appendChild(doc.createTextNode(value));
        hearingCollection.appendChild(hearingElement);
        return this;
    }

    public XMLWriter addMajorStatusCollection(){
        appealCollection.appendChild(majorStatusCollection);
        return this;
    }

    public XMLWriter addPartiesCollection(){
        appealCollection.appendChild(partiesCollection);
        return this;
    }

    public XMLWriter addHearingCollection(){
        appealCollection.appendChild(hearingCollection);
        return this;
    }

    public XMLWriter pushAppealToBeWritten(Appeal appeal){
        final Appeal appealToBeWritten = appeal;
        appealsToBeWritten.add(appealToBeWritten);
        return this;
    }

    public List<Appeal> getAppealWrittenToFile(){
        return appealsToBeWritten;
    }

    public XMLWriter addRecordsCollection(){

        Element numOfCases = doc.createElement("NumberOfCases");
        Integer records = appealsToBeWritten.size();
        numOfCases.appendChild(doc.createTextNode(String.valueOf(records)));

        Element recordsCollection = doc.createElement("Num_Records");
        recordsCollection.appendChild(numOfCases);
        appealsCollection.appendChild(recordsCollection);
        return this;

    }

    public XMLWriter addSchemaVersionCollection(){

//        Element version = doc.createElement("Version");
//    //    version.setAttributeNS("http://www.w3.org/2001/", "xsi:type", "xs:string");
////    //    version.setAttributeNS("http://www.w3.org/2001/", "xs:string", "http://www.w3.org/2001/XMLSchema");
//        version.setAttributeNS("http://www.w3.org/2001/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
//        version.appendChild(doc.createTextNode("8.0"));
//        schemaVersion = doc.createElement("Schema_Version");
//        schemaVersion.appendChild(version);
//        appealsCollection.appendChild(schemaVersion);
        return this;
    }


    public XMLWriter writeXML(String OUTPUT_DIR) throws TransformerException, IOException, ParserConfigurationException {

        String dateTime = now().format(ofPattern("yyyy-MM-dd-HH-mm-ss"));
        String fileName = "SSCS_" + path + "_Delta_" + dateTime + ".xml";
        Path path = get(OUTPUT_DIR + "/" + fileName);

        if (path.getParent() != null) {
            createDirectories(path.getParent());
        }

        writeDoc(path.toString(), doc);

        return this;

    }

//    public XMLWriter writePreUpdate(String filePath) throws TransformerException {
//        preUpdate = doc;
//        writeDoc(filePath, preUpdate);
//        return this;
//    }

    private XMLWriter writeDoc(String path, Document doc) throws TransformerException, IOException, ParserConfigurationException {

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

        Writer out = new StringWriter();

        StreamResult result = new StreamResult(new File(path));

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);
        // write the content into xml file

        transformer.transform(source, result);
        System.out.println("File saved!");

        return this;

    }

}
