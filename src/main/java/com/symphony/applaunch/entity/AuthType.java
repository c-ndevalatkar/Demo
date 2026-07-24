package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "auth_type")
public class AuthType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_type_id_seq")
    @SequenceGenerator(name = "auth_type_id_seq", sequenceName = "auth_type_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Integer id;

    @Column(name = "type")
    private String type;

    @Column(name = "is_default")
    private Boolean isDefault;

}