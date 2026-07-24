package com.symphony.applaunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NamedQuery;

import java.util.Date;

@NamedQuery(name = "UserLogins.getAllUserLoginsCount", query = "select count(u) FROM UserLogin u")
@NamedQuery(name = "UserLogin.getLoggedInUser", query = "FROM UserLogin login WHERE login.authToken = :token AND login.status = 'logged in' and login.logoutDate is null")
@Entity
@Getter
@Setter
@Table(name = "user_login")
public class UserLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_login_id_seq")
    @SequenceGenerator(name = "user_login_id_seq", sequenceName = "user_login_id_seq", allocationSize = 1)

    @Column(name = "user_login_id")
    private Long userLoginId;

    @Column(name = "username")
    private String username;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "login_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginDate;

    @Column(name = "logout_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logoutDate;

    @Column(name = "status")
    private String status;

    @Column(name = "company")
    private String company;

    @Column(name = "auth_token")
    private String authToken;

    @Transient
    private String firstName;

    @Transient
    private String lastName;

    @Column(name = "is_login")
    private Boolean isLogin;

}
