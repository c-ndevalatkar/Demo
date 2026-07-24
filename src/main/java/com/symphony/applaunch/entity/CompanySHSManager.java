package com.symphony.applaunch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NamedQuery;

@Setter
@Getter
@NamedQuery(name = "CompanySHSManager.findSHAManagerByCompany", query = "FROM CompanySHSManager company WHERE company.company.id =:company")
@Entity
@Table(name = "\"company_shsmanager\"")
public class CompanySHSManager {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_shsmanager_id_seq")
    @SequenceGenerator(name = "company_shsmanager_id_seq", sequenceName = "company_shsmanager_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;


}
