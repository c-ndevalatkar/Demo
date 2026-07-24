package com.symphony.applaunch.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.symphony.applaunch.util.CustomJsonDateDeserializer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "\"userApps\"")
public class UserApp {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userapps_id_seq")
    @SequenceGenerator(name = "userapps_id_seq", sequenceName = "userapps_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "\"userId\"")
    private Long userId;

    @Column(name = "\"companyId\"")
    private Integer companyId;

    @Column(name = "market_id")
    private Long marketId;

    @ManyToOne
    @JoinColumn(name = "\"shsAppId\"")
    private SHSApp shsApp;

    @Column(name = "\"subscriptionFrequency\"")
    private String subscriptionFrequency;

    @Column(name = "\"subscriptionStatus\"")
    private String subscriptionStatus;

    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    @Column(name = "\"expirationDate\"")
    private Date expirationDate;

    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    @Column(name = "\"startDate\"")
    private Date startDate = new Date();

    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdDate\"")
    private Date createdDate;

    @Column(name = "\"isMigrated\"")
    private Boolean isMigrated = false;

    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    @Transient
    private Long docCount;

    @Transient
    private Long userSubsCount;

    @Transient
    private Long atmddId;

    @Transient
    private String adUserName;

    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"modifiedDate\"")
    private Date modifiedDate;

    @ManyToOne
    @JoinColumn(name = "\"modifiedBy\"")
//	@Column(name = "\"modifiedBy\"")
    private Users modifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    private Users assignedBy;

    @Transient
    private Long userAppDashboard;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + companyId;
        result = prime * result + ((shsApp == null) ? 0 : shsApp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserApp other = (UserApp) obj;
        if (companyId != other.companyId)
            return false;
        if (shsApp == null) {
            if (other.shsApp != null)
                return false;
        } else if ((!Objects.equals(shsApp.getId(), other.shsApp.getId())))
            return false;

        return true;
    }

}
