package com.symphony.applaunch.dto;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogEventTypeDTO {

    private Integer id;

    private String name;

    private String description;

    private String reference;
}
