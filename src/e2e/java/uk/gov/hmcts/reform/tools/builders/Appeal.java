package uk.gov.hmcts.reform.tools.builders;

import java.io.IOException;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import uk.gov.hmcts.reform.tools.utils.*;

public class Appeal {

    public Date lastUpdatedDate = new Date();

    public AppealHeader header = new AppealHeader();
    public AppealParties parties = new AppealParties();
    public AppealMajorStatus majorStatus = new AppealMajorStatus();
    public AppealHearing hearing = new AppealHearing();

    private XMLWriter xmlWriter;
    private Map<String, String> appealHeaderStore = new HashMap<>();
    private List<Map<String, String>> appealPartiesStore = new ArrayList<>();
    private List<Map<String, String>> appealMajorStatusList = new ArrayList<>();
    private List<Map<String, String>> appealHearingStore = new ArrayList<>();
    private Date prevDate = new Date();
    private Integer daysOffSet = 0;

    public Appeal(XMLWriter xmlWriter) throws ParserConfigurationException {
        this.xmlWriter = xmlWriter;
    }

    public void setUpdatedDaysAGo(Integer offset) {
        lastUpdatedDate = TestContainer.backDate(new Date(), offset);
    }

    public void setStartedDaysAGo(Integer offSet) {
        daysOffSet = offSet;
        prevDate = TestContainer.backDate(lastUpdatedDate, offSet);
    }

    public String getLastUpdatedDate() {
        return TestContainer.asGapsDate(lastUpdatedDate);
    }

    public String getFutureDate(Integer offSet) {
        daysOffSet = offSet;
        Date futureDate = TestContainer.backDate(lastUpdatedDate, -offSet);
        return TestContainer.asGapsDate(futureDate);
    }

    public String getCaseId(){
        return header.getAppealCaseId();
    }

    public String getCaseRefNum(){
        return header.getAppealCaseRefNum();
    }


    public String getCaseRefNumber(){
        return header.getAppealCaseRefNum();
    }

    public String onDay(Integer daysAdded) {
        Date date = TestContainer.backDate(lastUpdatedDate, daysOffSet - daysAdded);
        String value = TestContainer.asGapsDate(date);
        return value;
    }

    public Appeal storeHeader() {
        appealHeaderStore = header.build();
        return this;
    }

    public Appeal storeParties() {
        appealPartiesStore.add(parties.build());
        parties = new AppealParties();
        return this;
    }

    public Appeal storeMajorStatus() {
        appealMajorStatusList.add(majorStatus.build());
        majorStatus = new AppealMajorStatus();
        return this;
    }

    public Appeal storeHearing() {
        appealHearingStore.add(hearing.build());
        return this;
    }

    public Appeal setLastUpdatedDate(Date date) {
        lastUpdatedDate = date;
        return this;
    }

    public XMLWriter write() throws ParserConfigurationException, TransformerException, IOException {
        this.xmlWriter.setAppealCollection();
        this.xmlWriter.setHeader(appealHeaderStore);
        this.xmlWriter.setParties(appealPartiesStore);
        this.xmlWriter.setHearing(appealHearingStore);
        this.xmlWriter.setMajorStatuses(appealMajorStatusList);
        return this.xmlWriter;
    }

}
