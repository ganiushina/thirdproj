package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class UserBonusKPIMain {    private int userId;
    private String fio;
    private String position;

    List<UserBonusKPIDetail> userBonusKPIDetails;
}
