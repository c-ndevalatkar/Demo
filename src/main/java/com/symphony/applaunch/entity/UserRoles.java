package com.symphony.applaunch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"userRoles\"")
public class UserRoles {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_role_id_seq")
    @SequenceGenerator(name = "user_role_id_seq", sequenceName = "user_role_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Integer id;

    @Column(name = "type")
    private String type;

    @Column(name = "\"typeCode\"")
    private String typeCode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

}
