package com.symphony.applaunch.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * @author Bushera.Hannure
 *
 */
@Entity
@Table(name = "dimension")
public class Dimension {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dimensions_id_seq")
	@SequenceGenerator(name = "dimensions_id_seq", sequenceName = "dimensions_id_seq", allocationSize = 1)

	// @Id
	// @GenericGenerator(name="generator", strategy="increment")
	// @GeneratedValue(generator="generator")
	@Column(name = "id")
	private Long id;

	@Column(name = "dimension_name")
	private String dimensionName;

	@Column(name = "description")
	private String description;

	@Column(name = "color")
	private String color;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTimestamp;

	@Column(name = "last_updated_by")
	private String lastUpdatedBy;

	@Column(name = "last_updated_timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdatedTimestamp;

	@Column(name = "instance_count")
	private Integer instanceCount;

	@Column(name = "override_count")
	private Integer overrideCount;

	@Column(name = "error_count")
	private Integer errorCount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDimensionName() {
		return dimensionName;
	}

	public void setDimensionName(String dimensionName) {
		this.dimensionName = dimensionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public Date getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}

	public void setLastUpdatedTimestamp(Date lastUpdatedTimestamp) {
		this.lastUpdatedTimestamp = lastUpdatedTimestamp;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Integer getInstanceCount() {
		return instanceCount;
	}

	public void setInstanceCount(Integer instanceCount) {
		this.instanceCount = instanceCount;
	}

	public Integer getOverrideCount() {
		return overrideCount;
	}

	public void setOverrideCount(Integer overrideCount) {
		this.overrideCount = overrideCount;
	}

	public Integer getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}
}
