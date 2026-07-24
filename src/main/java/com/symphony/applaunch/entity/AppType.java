package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "\"appTypes\"")
public class AppType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "apptype_id_seq")
    @SequenceGenerator(name = "apptype_id_seq", sequenceName = "apptype_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "logopath")
    private String logopath;

    @Column(name = "\"isMigrated\"")
    private Boolean isMigrated = false;

    @Column(name = "is_internal")
    private Boolean isInternal = false;

    @Column(name = "\"isActive\"")
    private Boolean isActive = false;

    @Column(name = "ad_id")
    private Integer adid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private Users lastUpdatedBy;

    @Column(name = "last_updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedDate;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @Transient
    private Long atmddId;

    @Transient
    private String type1;

    @Transient
    private Boolean isPinned;

    @Transient
    public Long appCount;

    @Transient
    public Long docCount;

    @Transient
    public Long userSubsCount;

    @Column(name = "color")
    private String color;

    // SHSP-CR-0001
    // display name to change apptype name to custom name
    @Column(name = "display_name")
    private String displayName;

    public void setAppcount(Long appcount) {
        this.appCount = appcount;
    }

}
