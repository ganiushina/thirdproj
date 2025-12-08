package ru.alta.thirdproj.entites;

import lombok.Data;

@Data
public class FailedProbationParticipant {
    private ParticipantType type;
    private String departmentName;
    private String responsibleUserName;
    private Double summResponsibleUser;
    private Double percentResponsibleUserByCandidatePercent;
    private String resecherDepartmentName;
    private String resecherName;
    private Double summResecher;
    private Double percentResecherByCandidatePercent;

    public enum ParticipantType {
        CONSULTANT,
        RESEARCHER
    }
}
