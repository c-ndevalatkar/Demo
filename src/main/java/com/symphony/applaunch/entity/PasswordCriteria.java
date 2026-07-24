package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "\"passwordCriterias\"")
public class PasswordCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "password_criteria_seq")
    @SequenceGenerator(name = "password_criteria_seq", sequenceName = "password_criteria_seq", allocationSize = 1)

    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "regex")
    private String regex;

    @Column(name = "is_default")
    private Boolean isdefault;

    @Transient
    private Boolean isconfigured;

    @Column(name = "is_active")
    private Boolean isActive;

}
