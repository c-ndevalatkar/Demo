package com.symphony.applaunch.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.hibernate.annotations.NamedQuery;

@NamedQuery(name = "MarketUser.getAllMarketUsersCount", query = "select count(m) FROM MarketUser m")
@NamedQuery(name = "MarketUser.getMarketByUser", query = "SELECT m.market FROM MarketUser m where m.user.id =:user")
@NamedQuery(name = "MarketUser.getUsersByMarket", query = "SELECT m.user FROM MarketUser m where m.market.id =:market")
@NamedQuery(name = "MarketUser.getMarketUserByUser", query = "SELECT m FROM MarketUser m where m.user.id =:user")
@Entity
@Table(name = "market_user")
public class MarketUser {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "market_user_id_seq")
	@SequenceGenerator(name = "market_user_id_seq", sequenceName = "market_user_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "market_id")
	private Market market;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_updated_by")
	private Users lastUpdatedBy;

	@Column(name = "created_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate = new Date();

	@Column(name = "last_updated_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdatedDate = new Date();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public Users getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Users createdBy) {
		this.createdBy = createdBy;
	}

	public Users getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Users lastUpdatedBy) {
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
