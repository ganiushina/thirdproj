package ru.alta.thirdproj.entites;

import lombok.Data;
import java.util.List;

@Data
public class FailedProbationActUpdate {
    private Integer actId;
    private Double totalNoNds;
    private String candidate;
    private String departmentName;
    private List<ActByUserCheck> participants;
}
