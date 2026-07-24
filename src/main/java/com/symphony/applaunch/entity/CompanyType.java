package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.NamedQuery;

import java.util.Date;

@NamedQuery(name = "CompanyType.getAllCompanyTypes", query = "FROM CompanyType")
@NamedQuery(name = "CompanyType.getAllCompanyTypesCount", query = "select count(*) FROM CompanyType")
@Entity
@Table(name = "\"companyTypes\"")
public class CompanyType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comptype_id_seq")
    @SequenceGenerator(name = "comptype_id_seq", sequenceName = "comptype_id_seq", allocationSize = 1)

    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "last_updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

}
