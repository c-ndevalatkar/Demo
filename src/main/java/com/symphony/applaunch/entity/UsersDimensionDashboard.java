package com.symphony.applaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_dimension_dashboard")
public class UsersDimensionDashboard {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_dim_dash_id_seq")
	@SequenceGenerator(name = "user_dim_dash_id_seq", sequenceName = "user_dim_dash_id_seq", allocationSize = 1)
	
	@Column(name = "id")
	private Long id;

	@Column(name = "dimension_id")
	private Long dimension;

	@Column(name = "user_id")
	private Long user;

	@Column(name = "dimension_name")
	private String dimensionName;

	public String getDimensionName() {
		return dimensionName;
	}

	public void setDimensionName(String dimensionName) {
		this.dimensionName = dimensionName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDimension() {
		return dimension;
	}

	public void setDimension(Long dimension) {
		this.dimension = dimension;
	}

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}
}
