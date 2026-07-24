package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "frequencies")
public class Frequencies {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "frequencies_id_seq")
    @SequenceGenerator(name = "frequencies_id_seq", sequenceName = "frequencies_id_seq", allocationSize = 1)
    // @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

}
