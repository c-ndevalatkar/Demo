package com.symphony.applaunch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NamedQuery;

import java.util.Date;
import java.util.List;

@NamedQuery(name = "Users.findByEmail", query = "FROM Users user WHERE lower(user.email) = :emailId")
@NamedQuery(name = "Users.findByCompanyId", query = "FROM Users user WHERE user.company.id = :company")
@NamedQuery(name = "Users.findOne", query = "FROM Users Where id= :id")
@NamedQuery(name = "Users.findByAdName", query = "FROM Users user WHERE lower(user.adUserName) = :adName")
@NamedQuery(name = "Users.findByEmailAndAdUserName", query = "FROM Users user WHERE lower(user.email) = :emailId and lower(user.adUserName) = :adName")

@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "users")
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "\"firstName\"")
    private String firstName;

    @Column(name = "\"lastName\"")
    private String lastName;

    @Column(name = "\"jobTitle\"")
    private String jobTitle;

    @Column(name = "email")
    private String email;

    @Column(name = "\"phoneNumber\"")
    private String phoneNumber;

    @Column(name = "language")
    private String language;

    @Column(name = "\"isTokenVerified\"", columnDefinition = "boolean default false", nullable = true)
    private Boolean isTokenVerified = false;

    @Column(name = "\"isActive\"")
    private Boolean isActive = false;

    @Column(name = "\"shsAdmin\"")
    private Boolean shsAdmin;

    @Transient
    private String fullName;

    @Column(name = "\"mailStatusFlg\"")
    private Boolean mailStatusFlg;

    @ManyToOne
    @JoinColumn(name = "company")
    private Company company;

    @Transient
    private Company oldCompany;

    @Transient
    private List<String> dimensions;

    @Transient
    private List<DimensionDTO> mdmDimensions;

    @ManyToOne
    @JoinColumn(name = "role")
    private UserRoles role;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "\"isAccountLocked\"", nullable = true)
    private Boolean isAccountLocked = false;

    @Column(name = "\"wrongPasswordCount\"")
    private Integer wrongPasswordCount = 0;

    @Column(name = "\"oneTimePassword\"")
    @JsonIgnore
    private String oneTimePassword;

    @Column(name = "\"ad_username\"")
    private String adUserName;

    @Column(name = "last_password_changed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPasswordChange;

    @Column(name = "token_generated_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tokenGeneratedTimestamp;

    @Column(name = "token")
    @JsonIgnore
    private String token;

    @Column(name = "is_internal")
    private Boolean isInternal = false;

    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date();

    @Transient
    private Boolean isDatasteward = false;

    @Column(name = "token_type")
    private String tokenType;

    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate = new Date();

    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "activation_expiration")
    @Temporal(TemporalType.TIMESTAMP)
    private Date activationExpiration;


    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;

    @Column(name = "last_updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedDate;

    @Transient
    private String type;

    @Transient
    private String emailSubject;

    @Transient
    private String url;

    @Transient
    private Boolean sendVerificationMail = false;

    @Transient
    private String ssoAppId;


    @Column(name = "last_date_verification_mail_sent")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDateVerificationMailSent;

    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;

    @Transient
    private Date oldEndDate;

    @Transient
    private String profile;

    @Transient
    private Long userAppId;

    @Column(name = "is_email_opted")
    private Boolean isEmailOpted = false;

    @Column(name = "is_terms_accepted")
    private Boolean isTermsAccepted = false;

    public Users() {
        super();

    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((adUserName == null) ? 0 : adUserName.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());

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
        Users other = (Users) obj;
        if (adUserName == null) {
            if (other.adUserName != null)
                return false;
        } else if (!adUserName.equals(other.adUserName))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
