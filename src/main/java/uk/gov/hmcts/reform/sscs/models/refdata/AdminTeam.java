package uk.gov.hmcts.reform.sscs.models.refdata;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminTeam {

    private Integer adminTeamId;
    private String admName;
    private LocalDateTime admClosureDate;
    private Integer venueId;
    private String adminTeamCode;
    private String adminTeamCode2;

}
