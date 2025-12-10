package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FailedProbationAct {
    private Integer actId;
    private String responsibleUserName;
    private Integer responsibleUserId;
    private String resecherName;
    private Integer resecherId;
    private Double summResponsibleUser;
    private Double summResecher;
    private String depatment;
    private Integer depatmentId;
    private Double percentResponsibleUserComplicity;
    private Double percentResecherComplicity;
    private String teameLeaderName;
    private Integer teameLeaderId;
    private String cityResponsibleUser;
    private Integer cityResponsibleUserId;
    private Double percentResponsibleUserByCandidatePercent;
    private Double percentReseacherByCandidatePercent;
    private String depatmentResecher;
    private Integer depatmentResecherId;
    private LocalDateTime dateUpdate;
}
