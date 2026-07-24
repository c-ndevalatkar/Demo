package com.symphony.applaunch.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import org.hibernate.annotations.NamedQuery;

@Entity
@Table(name = "user_mdm_dimension")
@NamedQuery(name = "UserMdmDimension.getMdmUsersCount", query = "select count(u) FROM UserMdmDimension u where dimension.id =:dimensionId ")
public class UserMdmDimension {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_mdm_dimension_id_seq")
	@SequenceGenerator(name = "user_mdm_dimension_id_seq", sequenceName = "user_mdm_dimension_id_seq", allocationSize = 1)

	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "mdm_dimension_id")
	private MdmDimension dimension;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private Users user;

	@Column(name = "default_filter")
	private String defaultFilter;

	@ManyToOne
	@JoinColumn(name = "created_by_id")
	private Users createdById;

	@Column(name = "created_timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTimestamp;

	@ManyToOne
	@JoinColumn(name = "last_updated_by_id")
	private Users lastUpdatedById;

	@Column(name = "status")
	private String status;

	@Column(name = "last_updated_timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdatedTimestamp;

	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

	@Transient
	private Long userId;
	@Transient
	private Long dimensionId;
	@Transient
	private Users modifiedBy;

	@ManyToOne
	@JoinColumn(name = "mdm_role")
	private MdmRole role;

	@Transient
	private String dimensionName;
	@Transient
	private String description;
	@Transient
	private String color;
	@Transient
	private Integer instanceCount;
	@Transient
	private Integer overrideCount;
	@Transient
	private Integer errorCount;
	@Transient
	private Integer roleId;

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public Users getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Users modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDimensionId() {
		return dimensionId;
	}

	public void setDimensionId(Long dimensionId) {
		this.dimensionId = dimensionId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MdmDimension getDimension() {
		return dimension;
	}

	public void setDimension(MdmDimension dimension) {
		this.dimension = dimension;
	}

	public String getDefaultFilter() {
		return defaultFilter;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public void setDefaultFilter(String defaultFilter) {
		this.defaultFilter = defaultFilter;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public Date getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}

	public Users getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Users createdById) {
		this.createdById = createdById;
	}

	public Users getLastUpdatedById() {
		return lastUpdatedById;
	}

	public void setLastUpdatedById(Users lastUpdatedById) {
		this.lastUpdatedById = lastUpdatedById;
	}

	public void setLastUpdatedTimestamp(Date lastUpdatedTimestamp) {
		this.lastUpdatedTimestamp = lastUpdatedTimestamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public MdmRole getRole() {
		return role;
	}

	public void setRole(MdmRole role) {
		this.role = role;
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
