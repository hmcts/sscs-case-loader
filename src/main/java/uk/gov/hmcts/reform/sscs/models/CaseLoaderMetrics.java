package uk.gov.hmcts.reform.sscs.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.apache.commons.lang.StringUtils;


public class CaseLoaderMetrics {

    private String fileName;
    private long fileSize;
    private long recordCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public long getFileSize() {
        return fileSize;
    }

    public void setRecordCount(long recordCount) {
        this.recordCount = recordCount;
    }
    
    public long getRecordCount() {
        return recordCount;
    }

    public void setStartTime() {
        this.startTime = LocalDateTime.now();
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setEndTime() {
        this.endTime = LocalDateTime.now();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getRunTime() {
        if (null != startTime && null != endTime) {
            long hours = ChronoUnit.HOURS.between(startTime, endTime);
            long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
            long seconds = ChronoUnit.SECONDS.between(startTime, endTime);

            return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        }

        return "";
    }

    public void merge(CaseLoaderMetrics metrics) {
        if (StringUtils.isNotEmpty(metrics.fileName)) {
            if (StringUtils.isNotEmpty(this.fileName)) {
                fileName += ", " + metrics.fileName;
            } else {
                fileName = metrics.fileName;
            }
        }

        fileSize += metrics.fileSize;

        recordCount += metrics.getRecordCount();

        if (null != metrics.getStartTime() && (null == startTime || metrics.getStartTime().isBefore(startTime))) {
            startTime = metrics.getStartTime();
        }

        if (null != metrics.getEndTime() && (null == endTime || metrics.getEndTime().isAfter(endTime))) {
            endTime = metrics.getEndTime();
        }
    }
}
