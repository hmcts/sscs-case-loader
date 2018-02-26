package uk.gov.hmcts.reform.sscs.models.refdata;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CaseCode {

    private Integer caseCodeId;
    private String ccdKey;
    private Integer ccdOralexmnTime;
    private Integer ccdPaperexmnTime;
    private LocalDateTime ccdLegnDate;
    private Integer ccdPreTime;
    private Integer ccdPostTime;
    private Integer benAssessTypeId;
    private Integer issuesId;
    private Integer sessionCategoryId;
    private Integer officeTypeId;
    private Integer businessRulesGrpId;

}
