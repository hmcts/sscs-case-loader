package uk.gov.hmcts.reform.sscs.models;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class CaseLoaderMetricsTest {

    @Test
    public void caseLoaderMetricsCanMerge() {
        String filename1 = "example1.file";
        String filename2 = "example2.file";
        long fileSize = 2048;
        long recordCount = 256;
        LocalDateTime earliestDate = LocalDateTime.of(2018, 1, 1, 1, 0, 0);
        LocalDateTime lessEarlyDate = LocalDateTime.of(2018, 1, 1, 2, 0, 0);
        LocalDateTime lessLateDate = LocalDateTime.of(2018, 1, 1, 3, 0, 0);
        LocalDateTime latestDate = LocalDateTime.of(2018, 1, 1, 4, 0, 0);

        CaseLoaderMetrics metrics = new CaseLoaderMetrics();

        CaseLoaderMetrics fullFileMetrics1 = new CaseLoaderMetrics();
        fullFileMetrics1.setFileName(filename1);
        fullFileMetrics1.setFileSize(fileSize);
        fullFileMetrics1.setRecordCount(recordCount);
        fullFileMetrics1.setStartTime(lessEarlyDate);
        fullFileMetrics1.setEndTime(lessLateDate);

        CaseLoaderMetrics fullFileMetrics2 = new CaseLoaderMetrics();
        fullFileMetrics2.setFileName(filename2);
        fullFileMetrics2.setFileSize(fileSize);
        fullFileMetrics2.setRecordCount(recordCount);
        fullFileMetrics2.setStartTime(earliestDate);
        fullFileMetrics2.setEndTime(latestDate);

        assertNull(metrics.getFileName());
        assertEquals(0, metrics.getFileSize());
        assertEquals(0, metrics.getRecordCount());
        assertNull(metrics.getStartTime());
        assertNull(metrics.getEndTime());
        assertEquals("", metrics.getRunTime());

        // Merge first full metrics
        metrics.merge(fullFileMetrics1);

        assertTrue(filename1.equals(metrics.getFileName()));
        assertEquals(fileSize, metrics.getFileSize());
        assertEquals(recordCount, metrics.getRecordCount());
        assertEquals(lessEarlyDate, metrics.getStartTime());
        assertEquals(lessLateDate, metrics.getEndTime());
        assertEquals("1h 0m 0s", metrics.getRunTime());

        // Merge second full metrics
        metrics.merge(fullFileMetrics2);

        assertTrue(StringUtils.contains(metrics.getFileName(), filename1));
        assertTrue(StringUtils.contains(metrics.getFileName(), filename2));
        assertEquals(fileSize*2, metrics.getFileSize());
        assertEquals(recordCount*2, metrics.getRecordCount());
        assertEquals(earliestDate, metrics.getStartTime());
        assertEquals(latestDate, metrics.getEndTime());
        assertEquals("3h 0m 0s", metrics.getRunTime());
    }
}