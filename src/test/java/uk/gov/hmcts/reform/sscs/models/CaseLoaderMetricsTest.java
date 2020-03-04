package uk.gov.hmcts.reform.sscs.models;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class CaseLoaderMetricsTest {

    static final String filename1 = "example1.file";
    static final String filename2 = "example2.file";
    static final long fileSize = 2048;
    static final long recordCount = 256;
    final LocalDateTime earliestDate = LocalDateTime.of(2018, 1, 1, 1, 0, 0);
    final LocalDateTime lessEarlyDate = LocalDateTime.of(2018, 1, 1, 2, 0, 0);
    final LocalDateTime lessLateDate = LocalDateTime.of(2018, 1, 1, 3, 0, 0);
    final LocalDateTime latestDate = LocalDateTime.of(2018, 1, 1, 4, 0, 0);

    @Test
    public void caseLoaderMetricsCanMerge() {
        CaseLoaderMetrics metrics = new CaseLoaderMetrics();

        assertNull(metrics.getFileName());
        assertEquals(0, metrics.getFileSize());
        assertEquals(0, metrics.getRecordCount());
        assertNull(metrics.getStartTime());
        assertNull(metrics.getEndTime());
        assertEquals("", metrics.getRunTime());

        // Merge first full metrics
        CaseLoaderMetrics fullFileMetrics1 = new CaseLoaderMetrics();
        fullFileMetrics1.setFileName(filename1);
        fullFileMetrics1.setFileSize(fileSize);
        fullFileMetrics1.setRecordCount(recordCount);
        fullFileMetrics1.setStartTime(lessEarlyDate);
        fullFileMetrics1.setEndTime(lessLateDate);

        metrics.merge(fullFileMetrics1);

        assertTrue(filename1.equals(metrics.getFileName()));
        assertEquals(fileSize, metrics.getFileSize());
        assertEquals(recordCount, metrics.getRecordCount());
        assertEquals(lessEarlyDate, metrics.getStartTime());
        assertEquals(lessLateDate, metrics.getEndTime());
        assertEquals("1h 0m 0s", metrics.getRunTime());

        // Merge second full metrics
        CaseLoaderMetrics fullFileMetrics2 = new CaseLoaderMetrics();
        fullFileMetrics2.setFileName(filename2);
        fullFileMetrics2.setFileSize(fileSize);
        fullFileMetrics2.setRecordCount(recordCount);
        fullFileMetrics2.setStartTime(earliestDate);
        fullFileMetrics2.setEndTime(latestDate);

        metrics.merge(fullFileMetrics2);

        assertTrue(StringUtils.contains(metrics.getFileName(), filename1));
        assertTrue(StringUtils.contains(metrics.getFileName(), filename2));
        assertEquals(fileSize * 2, metrics.getFileSize());
        assertEquals(recordCount * 2, metrics.getRecordCount());
        assertEquals(earliestDate, metrics.getStartTime());
        assertEquals(latestDate, metrics.getEndTime());
        assertEquals("3h 0m 0s", metrics.getRunTime());
    }
}
