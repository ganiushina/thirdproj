package ru.alta.thirdproj.entites;

import lombok.Data;

@Data
public class FailedProbationActUpdate {
    private Integer actId;
    private Double totalNoNds;
    private String candidate;
    private String departmentName;
    private java.util.List<FailedProbationParticipant> participants;
}
