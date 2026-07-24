package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "app_features")
public class Features {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_features_id_seq")
    @SequenceGenerator(name = "app_features_id_seq", sequenceName = "app_features_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Transient
    private Boolean selected = false;

    @Transient
    private Boolean isUpdateRequest;

    @Transient
    private Long userAppFeature;

    @Transient
    private Boolean companylevel = false;

    @Transient
    private Boolean userlevel = false;

    @Transient
    private Boolean marketlevel = false;

    @Transient
    private String accessLevel;

    @Column(name = "is_active")
    private Boolean isActive = true;

}
