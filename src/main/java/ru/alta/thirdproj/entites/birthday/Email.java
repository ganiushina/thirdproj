package ru.alta.thirdproj.entites.birthday;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Email {
    private String from;
    private List<String> to;
    private String subject;
    private String content;
    private Map <String, Object> properties;
    private String template;

}
