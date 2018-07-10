package uk.gov.hmcts.reform.tools.builders;

import java.util.*;
import uk.gov.hmcts.reform.tools.utils.TestContainer;
import uk.gov.hmcts.reform.tools.utils.XmlWriter;

public class Appeal {

    public Date lastUpdatedDate = new Date(3736886400000L); // Tuesday, June 1, 2088 12:00:00 AM

    public AppealHeader header = new AppealHeader();
    public AppealParties parties = new AppealParties();
    public AppealMajorStatus majorStatus = new AppealMajorStatus();
    public AppealHearing hearing = new AppealHearing();

    private final XmlWriter xmlWriter;
    private Map<String, String> appealHeaderStore = new HashMap<>();
    private final List<Map<String, String>> appealPartiesStore = new ArrayList<>();
    private final List<Map<String, String>> appealMajorStatusList = new ArrayList<>();
    private final List<Map<String, String>> appealHearingStore = new ArrayList<>();
    private Integer daysOffSet = 0;

    public Appeal(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    public void setUpdatedDaysAGo(Integer offset) {
        lastUpdatedDate = TestContainer.backDate(new Date(), offset);
    }

    public void setStartedDaysAGo(Integer offSet) {
        daysOffSet = offSet;
        TestContainer.backDate(lastUpdatedDate, offSet);
    }

    public String getLastUpdatedDate() {
        return TestContainer.asGapsDate(lastUpdatedDate);
    }

    public String getFutureDate(Integer offSet) {
        daysOffSet = offSet;
        Date futureDate = TestContainer.backDate(lastUpdatedDate, -offSet);
        return TestContainer.asGapsDate(futureDate);
    }

    public String onDay(Integer daysAdded) {
        Date date = TestContainer.backDate(lastUpdatedDate, daysOffSet - daysAdded);
        return TestContainer.asGapsDate(date);
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

    public XmlWriter write() {
        this.xmlWriter.setAppealCollection();
        this.xmlWriter.setHeader(appealHeaderStore);
        this.xmlWriter.setParties(appealPartiesStore);
        this.xmlWriter.setHearing(appealHearingStore);
        this.xmlWriter.setMajorStatuses(appealMajorStatusList);
        return this.xmlWriter;
    }

}
