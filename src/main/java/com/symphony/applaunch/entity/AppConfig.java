package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="app_config")
@Data
public class AppConfig {
    @Id
    private String id;
    private String name;
    private String type;
    private String url;

    @Column(name="config_json", columnDefinition = "text")
    private String configJson;
}
