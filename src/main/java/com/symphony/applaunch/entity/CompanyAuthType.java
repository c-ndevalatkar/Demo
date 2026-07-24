package com.symphony.applaunch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company_auth_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyAuthType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_auth_type_id_seq")
    @SequenceGenerator(name = "company_auth_type_id_seq", sequenceName = "company_auth_type_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Long id;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne
    @JoinColumn(name = "auth_type_id")
    private AuthType authType;

    @Column(name = "user_directory")
    private String userDirectory;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "is_automatic_user_offboarding")
    private Boolean isAutomaticUserOffboarding;

}
