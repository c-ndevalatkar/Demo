package com.symphony.applaunch.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DimensionFieldsDTO {

    private Long id;
    private Long dimensionId;
    private String fieldName;
    private String fieldLabel;
    private Boolean showInGrid;
    private Boolean showInFilter;
    private Boolean display;
    private Boolean readOnly;
    private Boolean required;
    private String dataType;
    private String title;
    private String fieldType;
    private String fieldOptions;
    private Boolean primary;
    private Boolean composite;
    private Integer fieldOrder;
    private Integer minLength;
    private Integer maxLength;

}
