package com.symphony.applaunch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.symphony.applaunch.util.CustomJsonDateDeserializer;
import com.symphony.applaunch.util.CustomJsonDateSerializer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NamedQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NamedQuery(name = "Company.findByNameIgnoreCase", query = "FROM Company company WHERE lower(company.name) = :companyName")
@NamedQuery(name = "Company.findOne", query = "FROM Company WHERE id= :id")
@NamedQuery(name = "Company.financeCodeExist", query = "FROM Company company WHERE company.financeCode =:financeCode")
@NamedQuery(name = "Company.getAllCompaniesCount", query = "select count(c) FROM Company c")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Setter
@Getter
@Table(name = "\"companies\"")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_id_seq")
    @SequenceGenerator(name = "company_id_seq", sequenceName = "company_id_seq", allocationSize = 1)

    @Column(name = "id")
    private Long id;

    @Column(name = "finanace_code", length = 25)
    private String financeCode;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "\"generalInfo\"")
    private String generalInfo;

    @Column(name = "\"siteURl\"")
    private String siteURl;

    @Column(name = "email")
    private String email;

    @Column(name = "\"contactNo\"")
    private String contactNo;

    @Column(name = "\"isActive\"")
    private Boolean isActive = false;

    @ManyToOne
    @JoinColumn(name = "\"companyType\"")
    private CompanyType companyType;

    @Column(name = "finance_code_id")
    private String financeCodeObj;

    @Column(name = "theme")
    private String theme = "theme-gray";

    @Column(name = "\"maxLoginRetries\"")
    private Long maxLoginRetries;

    @Column(name = "\"reuseCount\"")
    private Long reuseCount;

    @Column(name = "expiration")
    private Long expiration;

    @Column(name = "\"passwordStrength\"")
    private Long passwordStrength;

    @Column(name = "is_approved")
    private Boolean isApproved;

    // @ManyToOne
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "ad_name")
    private String adName;

    @Column(name = "client_finance_name")
    private String clientFinanceName;

    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    @JsonSerialize(using = CustomJsonDateSerializer.class)
    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate = new Date();

    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "Mainframe_Client_Code")
    private Long mainframeClientCode;

    @Column(name = "\"ad_companyId\"")
    private Long adCompanyId;

    // @ManyToOne(optional = true)
    @Column(name = "global_admin")
    private Long globalAdmin;

    // @ManyToOne

    @Transient
    String autoGenCompanyName;

    @Column(name = "client_gid")
    private String clientGID;

    @Transient
    private UserApp userApp;

    @Transient
    private Users engagementuser;

    @Transient
    private Users globaluser;

    @Transient
    private String globalAdminName;

    @Transient
    private Boolean engagementManagerName = false;

    @Column(name = "last_updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private Users lastUpdatedBy;

    @Transient
    List<Users> engagementManagerList = new ArrayList<>();

    @Transient
    List<CompanySHSManager> companyShaManager = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "company")
    private CompanyAuthType companyAuthType;

    @Column(name = "is_multiple_session_allowed")
    private Boolean isMultipleSessionAllowed = false;

    @Transient
    private List<PasswordCriteria> passwordCriteria = new ArrayList<>();

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getFinanceCode() {
        return financeCode != null ? financeCode : "";
    }

    public void setFinanceCode(String financeCode) {
        if (financeCode != null)
            this.financeCode = financeCode.trim();
        else
            this.financeCode = financeCode;
    }

    public String getFinanceCodeObj() {
        return financeCodeObj != null ? financeCodeObj : "";
    }

    public void setFinanceCodeObj(String financeCodeObj) {
        if (financeCodeObj != null)
            this.financeCodeObj = financeCodeObj.trim();
        else
            this.financeCodeObj = financeCodeObj;
    }

}
