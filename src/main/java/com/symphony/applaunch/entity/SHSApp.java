package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NamedQuery;

import java.util.Date;
import java.util.List;

@NamedQuery(name = "SHSApp.findOne", query = "FROM SHSApp WHERE id= :id")
@NamedQuery(name = "SHSApp.findByNameIgnoreCase", query = "FROM SHSApp app WHERE lower(app.name) = :name")
@NamedQuery(name = "SHSApp.getCommonMenus", query = "FROM SHSApp app WHERE app.displayType = :displayType")
@NamedQuery(name = "SHSApp.findByNameAndUrl", query = "FROM SHSApp app WHERE lower(app.name) = :name or lower(app.url) =:url")
@NamedQuery(name = "SHSApp.getAllSHSAppsCount", query = "select count(sa) FROM SHSApp sa")
@Setter
@Getter
@Entity
@Table(name = "\"shsApps\"")
public class SHSApp {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shsapp_id_seq")
    @SequenceGenerator(name = "shsapp_id_seq", sequenceName = "shsapp_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "description")
    private String description;

    @Column(name = "color")
    private String color;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.JOIN)
    @JoinTable(name = "\"shsAppFrequencies\"", joinColumns = {
            @JoinColumn(name = "\"shsAppId\"", nullable = false)}, inverseJoinColumns = {
            @JoinColumn(name = "\"frequencyId\"", nullable = false)})
    private List<Frequencies> frequencies;

    @ManyToOne
    @JoinColumn(name = "\"appType\"")
    private AppType appType;

    @Column(name = "\"customAppType\"")
    private String customAppType;

    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @Column(name = "is_internal")
    private Boolean isInternal = false;

    @Transient
    private Long atmddId;

    @Transient
    private Boolean isPinned;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createddate = new Date();

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @Column(name = "internal_id")
    private String internalId;

    @Column(name = "supporting_group")
    private String supportingGroup;

    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "num_views")
    private Long numViews;

    @Column(name = "weight")
    private Long weight;

    @Column(name = "\"isMigrated\"")
    private Boolean isMigrated = false;

    @Column(name = "ad_id")
    private Long adId;

    @Transient
    private Boolean isActive;

    @Column(name = "display_type")
    private String displayType = "AP";

    @Column(name = "sequence")
    private Integer sequence = 0;

    @Column(name = "role_type_code")
    private String roleTypeCode;

    @Column(name = "server")
    private String server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private Users lastUpdatedBy;

    @Column(name = "last_updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedDate;

    @Transient
    private String subscriptionStatus;

    @Column(name = "shortcut_url")
    private String shortcutUrl;

    @Column(name = "default_auth_user")
    private String defaultAuthUser;

    // SHSP-CR-0001
    // display name to change apptype name to custom name
    @Column(name = "display_name")
    private String displayName;

    @Transient
    public Long docCount;

}
