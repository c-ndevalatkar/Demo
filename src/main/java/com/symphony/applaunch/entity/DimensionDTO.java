package com.symphony.applaunch.entity;

import com.symphony.applaunch.dto.DimensionFieldsDTO;
import com.symphony.applaunch.dto.InstanceValues;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DimensionDTO {

    private Long id;

    private String dimensionName;

    private String description;

    private String color;

    private Integer instanceCount;

    private Integer overrideCount;

    private Integer errorCount;

    private List<InstanceValues> values;

    private MdmRole role;

    private String primaryKey;

    private String dimensionJSON;

    private String status;

    private Long userDimensionId;

    private List<DimensionFieldsDTO> dimensionFieldsDTOs;

}
