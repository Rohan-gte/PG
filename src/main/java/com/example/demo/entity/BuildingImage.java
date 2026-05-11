package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "building_images", indexes = {
        @Index(name = "ix_building_images_building", columnList = "building_id")
})
public class BuildingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
