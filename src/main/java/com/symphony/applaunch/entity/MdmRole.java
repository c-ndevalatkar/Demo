package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "mdm_role")
public class MdmRole {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mdm_role_id_seq")
    @SequenceGenerator(name = "mdm_role_id_seq", sequenceName = "mdm_role_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Integer id;

    @Column(name = "role")
    private String role;

}
