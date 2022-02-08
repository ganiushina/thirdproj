package ru.alta.thirdproj.specification;

import lombok.Data;

@Data
public class SearchCriteria {
    private String key;
    private String operation;
    private Object value;

    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }
}


//    key: the field name – for example, firstName, age, … etc.
//    operation: the operation – for example, equality, less than, … etc.
//    value: the field value – for example, john, 25, … etc.
