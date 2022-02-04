package ru.alta.thirdproj.entites;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Data
public class User {


    private Long userId;

    private String userName;

    private String userFIO;

    private String userPosition;

    private int loginDepartment;

    private String password;


    private Collection<Role> roles;


    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("man_id", "userId");
        COLUMN_MAPPINGS.put("man_fio", "userFIO");
        COLUMN_MAPPINGS.put("login_name", "userName");
        COLUMN_MAPPINGS.put("user_position", "userPosition");
        COLUMN_MAPPINGS.put("user_department", "loginDepartment");
        COLUMN_MAPPINGS.put("login_hash_bcript", "password");


    }


    @Override
    public String toString() {
        return "User{" +  ", userName='" + userName + '\'' + ", password='" + "*********" + '\''
                + ", firstFIO='"  + userFIO +
                 '}';
    }
}
